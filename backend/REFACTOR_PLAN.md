# Backend 代码重构计划

## 1. 背景与依据

本文档基于 `backend` 当前代码结构，以及项目内 skill `backend/.agents/skills/alibaba-java-manual-skill` 对《阿里巴巴 Java 开发手册（嵩山版）》的约束整理。

本次计划不以一次性大改为目标，而是按风险和收益分阶段推进。重构过程中应遵循以下原则：

- 最小修改：优先处理违反规约、影响稳定性或阻碍测试的问题，不重写无关业务逻辑。
- 保持兼容：外部 API 路径、请求响应结构、数据库迁移历史不随意破坏。
- 先补测试再拆分：对诊断、模型配置、权限、安全等核心链路先建立回归测试，再做职责拆分。
- 明确规约等级：区分【强制】、【推荐】和项目工程化建议，避免把建议当作硬性规则。

## 2. 当前代码概览

后端技术栈：

- Spring Boot 3.3.5
- Java 17
- Spring Security + JWT
- MyBatis Plus
- Flyway + MySQL
- Log4j2
- Springdoc OpenAPI
- H2 测试依赖

主要模块：

- `auth`：登录、JWT、用户认证信息
- `user` / `role`：用户、角色、权限管理
- `diagnosis`：AI 排障会话、消息、结果
- `ai`：AI 客户端、AI 诊断服务、调用日志
- `model`：模型供应商配置
- `prompt`：Prompt 模板
- `risk`：风险命令检测
- `casebase` / `knowledge` / `report`：案例、知识库、报告
- `operationlog`：操作日志
- `common` / `config`：统一响应、异常、安全和 Web 配置

## 3. 主要问题清单

### 3.1 Service 职责过重

现状：

- `DiagnosisService` 约 681 行，包含会话创建、消息持久化、AI 调用编排、风险检测、结果转换、查询、状态更新、内容拼接和 JSON 序列化。
- `ModelConfigService`、`PromptTemplateService`、`KnowledgeService`、`CaseService` 等也承担了查询构造、实体填充、VO 转换和业务校验等多类职责。

命中规约：

- 编程规约 / 代码格式 /【推荐】：单个方法总行数不超过 80 行。
- 设计规约 / 应用分层 /【参考】：分层清晰，职责边界明确。

风险：

- 核心服务难以单测，修改局部逻辑容易引发回归。
- 业务编排、数据转换、持久化细节混在一起，后续新增能力成本较高。

### 3.2 魔法值与状态散落

现状：

- 多处直接使用 `"user"`、`"assistant"`、`"LOW"`、`"HIGH"`、`"CRITICAL"`、`0`、`1`、`"WAITING_CONFIRM"` 等状态和角色值。
- 数据库中也存在状态字段，如 `status`、`deleted`、`default_model`、`is_production`，代码里以整数直接判断。

命中规约：

- 编程规约 / 常量定义 /【强制】：不允许任何魔法值直接出现在代码中。
- 编程规约 / 常量定义 /【推荐】：固定范围变化的变量优先使用 enum 类型。

风险：

- 状态语义不集中，新增状态或调整文案时容易漏改。
- 字符串拼写错误无法在编译期发现。

### 3.3 POJO、实体与数据库类型需统一复核

现状：

- 数据库使用 `TINYINT` 表达布尔和状态，Java 实体中常见 `Integer` 表示。
- MyBatis Plus 逻辑删除字段统一配置为 `deleted`，各模块自行拼接 `.eq(..., 0)` 的逻辑较多。
- `api_key_encrypted` 字段名表示加密，但当前代码中直接读取并作为 API Key 使用，需确认是否真正加密。

命中规约：

- 编程规约 / OOP /【强制】：DO 类属性类型要与数据库字段类型相匹配。
- MySQL 数据库 / 建表规约 /【推荐】：表达是与否概念的字段建议使用 `is_xxx`，状态值应有清晰注释。
- 安全规约 / 敏感信息 /【强制/推荐】：敏感数据不得明文泄露，日志和存储需谨慎处理。

风险：

- 类型表达不一致会导致转换逻辑扩散。
- 敏感信息字段如果未加密，会形成生产安全隐患。

### 3.4 查询、转换、分页模式重复

现状：

- 多个 Service 重复编写 `LambdaQueryWrapper`、分页对象、`toSummary`、`toDetail`、软删除条件。
- Controller 和 Service 的返回结构基本统一，但缺少跨模块的基础查询/转换约定。

命中规约：

- 编程规约 / 注释规约 /【推荐】：删除未使用或低价值代码，提升可读性。
- 设计规约 / 应用分层 /【参考】：公共能力沉淀到合适层次。

风险：

- 重复逻辑增加维护成本。
- 软删除、状态过滤、排序规则容易不一致。

### 3.5 异常与错误码体系需要增强

现状：

- 已有 `BusinessException`、`ErrorCode`、`GlobalExceptionHandler`。
- 参数校验异常、业务异常和未知异常已有统一出口。
- 但部分业务仍使用通用错误码加自定义中文消息，缺少更细的错误码归类。

命中规约：

- 异常日志 / 错误码 /【强制】：错误码需要快速溯源、沟通标准化。
- 异常日志 / 异常处理 /【推荐】：区分 unchecked / checked 异常，避免随意抛通用运行时异常。

风险：

- 前端和运维定位问题时依赖自然语言消息，难以稳定匹配。
- 第三方 AI 调用失败的错误归因不够细。

### 3.6 日志与敏感数据治理仍需收敛

现状：

- 日志整体使用 SLF4J，占位符方式较规范。
- 已存在 `SensitiveDataMasker`，AI 调用异常中有脱敏处理。
- 但诊断文本、日志内容、命令输出、API Key、用户信息等敏感内容流经多个模块。

命中规约：

- 异常日志 / 日志规约 /【强制】：日志输出使用占位符；异常信息应包含现场信息和堆栈。
- 安全规约 / 敏感信息 /【强制/推荐】：避免敏感信息泄露。

风险：

- AI 原始响应、用户输入日志和排障命令可能包含密钥、IP、账号或内部路径。
- 需要定义哪些字段可入库、哪些字段必须脱敏后入库。

### 3.7 单元测试覆盖不足

现状：

- `src/test/java` 当前只有 `DevopsAiApplicationTests`。
- 项目已引入 Spring Boot Test、Spring Security Test 和 H2，但尚未形成核心业务测试集。

命中规约：

- 单元测试 /【强制】：单元测试应自动执行、独立、可重复。
- 单元测试 /【推荐】：核心模块覆盖语句和分支，数据库相关测试避免依赖已有数据。

风险：

- 重构缺少安全网。
- AI 解析、风险检测、权限校验、模型配置解析等高价值逻辑无法快速回归。

## 4. 重构目标

### 4.1 架构目标

- 领域服务保持业务编排职责，复杂转换、查询构造、外部调用适当拆分。
- 状态、角色、风险等级、启停标记统一常量或枚举表达。
- Controller 只负责 HTTP 入参、鉴权上下文和响应封装，不承载业务细节。
- Mapper 只负责持久化访问，不写业务判断。
- AI 调用、Prompt 渲染、风险检测、模型配置解析保持可独立测试。

### 4.2 质量目标

- 核心 Service 方法尽量控制在 80 行以内。
- 消除核心链路中的魔法字符串和魔法数字。
- 新增或调整代码必须有对应单测或集成测试。
- 统一错误码、日志字段和敏感信息脱敏策略。
- Maven `test` 可作为最小回归门禁。

## 5. 分阶段计划

## 阶段一：基线治理与测试安全网

优先级：P0

目标：

- 在不改变业务行为的前提下建立可回归基线。
- 明确重构前的接口、数据库和安全行为。

任务：

1. 运行并固化当前后端构建命令：
   - `mvn test`
   - `mvn spring-boot:run` 或项目现有启动方式
2. 为核心纯逻辑补单测：
   - `RiskDetectionService`
   - `RiskLevel`
   - `SensitiveDataMasker`
   - `AiClient` 的响应文本提取逻辑
   - `ModelConfigService.resolve`
3. 为安全和统一异常补 Web 层测试：
   - 未登录访问受保护接口返回统一错误结构。
   - 参数校验失败返回 `COMMON_PARAM_INVALID`。
   - 业务异常返回对应 HTTP 状态和错误码。
4. 导出当前 OpenAPI 或接口清单，作为重构兼容性基线。
5. 建立测试数据约定：
   - H2 测试配置独立。
   - 数据库测试自动回滚。
   - 不依赖本地 MySQL 现有数据。

验收标准：

- `mvn test` 稳定通过。
- 核心重构目标模块至少有基础单测。
- 重构前后接口路径、请求方法和响应字段保持一致。

## 阶段二：常量、枚举和状态语义收敛

优先级：P0

目标：

- 消除高频魔法值，提升编译期约束。

任务：

1. 新增或整理枚举：
   - `MessageRole`：`USER`、`ASSISTANT`、`SYSTEM`
   - `RiskLevel`：保留现有枚举并统一字符串转换入口
   - `RecordStatus` 或各领域状态枚举：启用、停用
   - `DeletedStatus` 或通用常量：未删除、已删除
   - `DiagnosisStatus`：`ANALYZING`、`WAITING_CONFIRM` 等
2. 替换核心模块中的直接字符串：
   - `DiagnosisService`
   - `AiClient`
   - `RiskDetectionService`
   - `CaseService`
   - `KnowledgeService`
3. 替换核心模块中的 `0` / `1` 状态判断：
   - 先通过常量替换，不急于改变数据库字段类型。
   - 后续如需要再引入 TypeHandler。
4. 补充状态转换单测，覆盖未知值和空值。

验收标准：

- 核心业务代码中不再直接散落角色、风险等级、删除状态等魔法值。
- 数据库存储值不变，接口返回值不变。
- 单测覆盖枚举和常量转换。

## 阶段三：拆分 `DiagnosisService`

优先级：P0

目标：

- 降低核心排障服务复杂度，使 AI 编排、会话持久化、消息构建和结果转换可分别测试。

建议拆分：

- `DiagnosisSessionService`
  - 创建会话
  - 查询会话
  - 更新状态
  - 软删除
- `DiagnosisMessageService`
  - 保存用户消息
  - 保存助手消息
  - 构建会话上下文
- `DiagnosisAnalysisOrchestrator`
  - 编排 AI 调用、风险检测、结果落库
- `DiagnosisResultAssembler`
  - `DiagnosisResult` 与 `AnalyzeResponse` 转换
- `DiagnosisPromptContentBuilder`
  - 构建用户内容、追问内容、重新分析内容

迁移步骤：

1. 先抽私有方法到包内组件，保持原公开方法签名不变。
2. 对 `analyze`、`continueAnalyze`、`reanalyze` 分别补回归测试。
3. 将查询类方法和命令类方法分开，避免事务范围覆盖外部 AI 调用时间过长。
4. 评估 AI 调用是否应放在事务外：
   - 当前事务包裹数据库写入和外部 AI 调用，外部调用慢或失败会拉长事务。
   - 建议改为先创建会话和用户消息，再调用 AI，最后单独事务保存结果。
5. 删除无用兜底或确认其用途：
   - `buildStubAnalysis` 如未被调用，应删除或改为明确的测试/降级路径。

验收标准：

- `DiagnosisService` 或对外门面类只保留清晰的业务入口。
- 单个方法尽量不超过 80 行。
- AI 调用失败时会话和消息状态明确，不产生半成品语义不清的数据。
- 原有 API 行为保持兼容。

## 阶段四：模型配置、Prompt 和 AI 客户端边界治理

优先级：P1

目标：

- 明确模型配置解析、Prompt 渲染、AI HTTP 调用和响应解析的职责边界。

任务：

1. `ModelConfigService` 拆出：
   - `ModelConfigResolver`
   - `ModelConfigValidator`
   - `ModelConfigAssembler`
2. `AiClient` 拆出：
   - 请求体构造器
   - 响应解析器
   - RestClient 工厂或配置组件
3. 明确 `api_key_encrypted`：
   - 如果确实加密，新增加解密组件和密钥配置。
   - 如果暂未加密，字段重命名需走 Flyway 迁移，或文档明确技术债。
4. 将 OpenAI Responses 风格请求中的 schema 常量化，避免在方法内维护大块嵌套 Map。
5. 第三方调用异常分级：
   - 超时
   - 认证失败
   - 响应格式错误
   - 供应商返回业务错误

验收标准：

- AI 响应解析可通过 JSON fixture 单独测试。
- 模型配置解析不依赖真实外部 AI 服务。
- API Key 不出现在日志、异常消息和普通接口响应中。

## 阶段五：通用 CRUD 模式收敛

优先级：P1

目标：

- 降低 `casebase`、`knowledge`、`prompt`、`model`、`category` 等模块重复代码。

任务：

1. 提取通用分页参数规范：
   - 页码小于 1 时后端兜底为 1。
   - 页大小设置上限，避免一次查询过大。
2. 提取软删除查询约定：
   - 通用常量 `NOT_DELETED` / `DELETED`。
   - 常用查询方法集中到领域 repository/helper，避免每个 Service 手写。
3. 提取 assembler：
   - `toSummary`
   - `toDetail`
   - `fillEntity`
4. Controller 层统一命名：
   - 创建、更新、删除、详情、分页查询接口保持一致的风格。
5. 对批量或列表接口补空集合返回测试。

验收标准：

- 重复查询和转换逻辑明显减少。
- 分页和软删除行为在各模块一致。
- 列表为空时返回空数组，不返回 `null`。

## 阶段六：异常、错误码和日志规范化

优先级：P1

目标：

- 提升问题定位能力，避免敏感信息泄露。

任务：

1. 复核 `ErrorCode`：
   - 按模块分段。
   - 保持 5 位字符串编码。
   - 不把错误等级、HTTP 状态写入错误码本身。
2. 统一异常消息策略：
   - 用户可见消息简洁。
   - 日志包含上下文 ID，如 `userId`、`sessionId`、`requestId`。
   - 第三方异常不直接透传给前端。
3. 操作日志和请求日志脱敏：
   - Authorization
   - API Key
   - password
   - token
   - 可能包含密钥的诊断文本
4. 检查日志配置：
   - 生产环境 debug 关闭。
   - 日志保留策略符合至少 15 天的要求。
   - 避免重复打印日志。

验收标准：

- 业务异常、参数异常、系统异常输出结构一致。
- 敏感字段在日志中不可见。
- 常见 AI 调用失败具备可定位错误码。

## 阶段七：数据库与迁移脚本治理

优先级：P2

目标：

- 确保实体、索引、字段语义和迁移脚本长期可维护。

任务：

1. 复核 DO 字段类型与数据库字段类型：
   - `BIGINT` 对 `Long`
   - `INT` 对 `Integer`
   - `DECIMAL` 对 `BigDecimal`
   - `DATETIME` 对 `LocalDateTime`
   - `TINYINT` 状态字段统一表达方式
2. 复核索引：
   - 高频查询条件是否有联合索引。
   - 排序字段是否与查询条件匹配。
   - 软删除字段是否需要纳入组合索引。
3. 复核 JSON 字段：
   - `result_json`
   - `commands`
   - `tags`
   - 明确存储结构和兼容策略。
4. 所有结构变更通过新增 Flyway 脚本完成，不修改已发布迁移。

验收标准：

- 实体和表结构有明确映射。
- 新增迁移可在空库和已有库上执行。
- 关键分页查询具备合理索引。

## 阶段八：工程结构和命名规范整理

优先级：P2

目标：

- 让包结构和类命名更符合分层语义。

建议结构：

```text
com.example.devopsai
  common
    exception
    response
    security
    support
  config
  auth
  diagnosis
    controller
    service
    assembler
    entity
    mapper
    dto
    vo
  ai
    client
    service
    dto
    vo
  model
    controller
    service
    assembler
    entity
    mapper
    dto
    vo
```

说明：

- 当前项目已按领域分包，不建议一次性大规模移动所有类。
- 可以从高复杂度模块 `diagnosis`、`ai`、`model` 先试点。
- 若移动包路径，需要同步调整测试、组件扫描和文档。

验收标准：

- 新增代码按新结构落位。
- 老代码迁移分模块进行，每次迁移后测试通过。
- 包名保持小写，类名和方法名符合 Java 命名规约。

## 6. 建议优先级路线图

第一周：

- 建立测试基线。
- 补 `RiskDetectionService`、`RiskLevel`、`SensitiveDataMasker`、`GlobalExceptionHandler` 基础测试。
- 提取核心常量和枚举。

第二周：

- 拆分 `DiagnosisService` 的消息、结果、内容构建职责。
- 调整 AI 调用事务边界。
- 补诊断主流程回归测试。

第三周：

- 拆分 `ModelConfigService` 和 `AiClient`。
- 明确 API Key 加密策略。
- 增加 AI 响应 fixture 测试。

第四周：

- 收敛 CRUD、分页、软删除模式。
- 复核错误码和日志脱敏。
- 复核数据库索引和 Flyway 迁移策略。

## 7. 重构期间的门禁规则

每次重构提交至少满足：

- `mvn test` 通过。
- 不修改已发布 Flyway 脚本，只新增迁移。
- 不改变既有 API 响应字段，除非同步更新前端和接口文档。
- 不在日志中输出明文密码、Token、API Key。
- 新增公开方法、复杂私有方法必须有测试覆盖或明确说明不可测原因。
- 删除代码前先确认无引用，避免删除未来降级路径或兼容入口。

## 8. 风险与回滚策略

主要风险：

- 拆分 `DiagnosisService` 时影响 AI 排障主流程。
- 改动事务边界后出现会话状态和结果状态不一致。
- 常量枚举替换过程中改变数据库存储值。
- 错误码调整影响前端判断。

控制策略：

- 每阶段小步提交。
- 先补测试，再做行为等价重构。
- 对外接口和数据库存储保持兼容。
- 对事务边界调整增加失败场景测试。
- 如发现行为偏差，优先回滚当前阶段改动，不跨阶段回滚。

## 9. 完成定义

本轮后端重构完成的最低标准：

- 核心模块具备稳定自动化测试。
- `DiagnosisService` 不再承担所有排障链路细节。
- 核心魔法值已集中为常量或枚举。
- AI 调用、模型配置解析、风险检测均可单独测试。
- 错误码和日志脱敏策略明确且有测试覆盖。
- 数据库迁移和实体映射经过一次系统复核。
