---
name: alibaba-java-development-manual
description: Use this skill when generating, reviewing, refactoring, or explaining Java/Spring/MySQL code according to the Alibaba Java Development Manual (Songshan edition). It helps Claude/Codex apply the manual's mandatory, recommended, and reference rules without changing their meaning.
---

# 阿里巴巴 Java 开发手册 Skill（嵩山版）

本 Skill 用于在 Claude/Codex 中按《阿里巴巴 Java 开发手册（嵩山版）》辅助 Java 代码生成、代码审查、重构建议、MySQL 建表/索引审查、异常日志审查、单元测试审查、安全规约审查和工程结构审查。

## 使用原则

1. 以原文为准：需要精确判断时，先查 `references/manual-full-text.md`，必要时再核对 `references/阿里巴巴Java开发规范（嵩山版）.pdf`。
2. 不改变规约含义：保留【强制】、【推荐】、【参考】的约束等级，不要把推荐项上升为强制项。
3. 最小修改：给出代码建议时，只改动违反规约或明显有风险的部分，不重写无关业务逻辑。
4. 明确来源：输出审查意见时，标明命中的章节、约束等级和关键词，例如“编程规约 / 命名风格 / 【强制】”。
5. 区分上下文：如果项目既有规范与手册冲突，先指出冲突，再给出兼容性建议，不直接覆盖项目约定。

## 资料文件

- `references/manual-full-text.md`：PDF 文本转写，按 PDF 页码组织，便于检索。
- `references/rule-index.md`：自动抽取的规约索引，便于快速定位【强制】、【推荐】、【参考】条目。
- `references/阿里巴巴Java开发规范（嵩山版）.pdf`：用户提供的原始 PDF，用于核对。
- `checklists/review-checklist.md`：审查输出格式和注意事项。

## 工作流：代码审查

收到 Java、Spring、MyBatis、SQL、建表语句、日志、异常处理、单元测试或工程结构相关内容时：

1. 识别审查范围：命名、常量、格式、OOP、日期时间、集合、并发、控制语句、注释、前后端、异常日志、单元测试、安全、MySQL、工程结构、设计规约。
2. 先匹配【强制】规约，再匹配【推荐】和【参考】规约。
3. 对每个问题输出：问题、命中规约、风险、最小修改建议。
4. 只在必要时给出修改后的代码片段；不要无依据地大面积重构。
5. 如果无法确认是否违反规约，说明“不确定”，并指出需要补充的上下文。

## 工作流：生成代码

生成 Java 代码时默认遵循：

- 类名 UpperCamelCase；方法、参数、成员变量、局部变量 lowerCamelCase；常量全大写并用下划线分隔。
- 缩进使用 4 个空格，不使用 Tab；单行尽量不超过 120 字符。
- 覆写方法添加 `@Override`。
- 包装类比较使用 `equals`；`BigDecimal` 等值比较使用 `compareTo`。
- 金额避免使用浮点类型；精确小数优先使用 `BigDecimal` 的字符串构造或 `valueOf`。
- 集合、并发、日期时间、异常日志、MySQL 相关代码必须继续查原文细则，不只依赖本摘要。

## 工作流：输出审查报告

建议输出模板：

```markdown
## 审查结论
通过 / 需要修改 / 存在风险

## 问题列表
1. 问题：...
   - 命中规约：...
   - 风险：...
   - 建议：...

## 修改示例
```java
// 只放必要片段
```
```

## 特别限制

- 不要声称本 Skill 覆盖了手册之外的行业规范。
- 不要把本 Skill 当作静态扫描器；没有完整工程上下文时，只能做文本级和语义级辅助审查。
- 不要删除原文中的“说明 / 正例 / 反例”含义；需要引用时应查完整转写或 PDF。
