# AI 运维排障助手

面向开发、运维、实施人员的 AI 排障平台。用户输入报错日志、命令输出或故障描述后，系统生成结构化排查建议、修复步骤、风险提示，并沉淀历史记录和案例。

创建人：Zhangzhenming

## 技术栈

- 前端：Vue 3 + TypeScript + Vite + Ant Design Vue
- 后端：Spring Boot 3 + MyBatis-Plus + Spring Security + JWT
- 数据库：MySQL 8
- 缓存：Redis
- 部署：Docker Compose

## 项目结构

```text
.
├── backend                 Spring Boot 后端
├── frontend                Vue3 前端
└── docker-compose.yml      本地依赖服务
```

## 本地启动

启动 MySQL 和 Redis：

```bash
docker compose up -d mysql redis
```

启动后端：

```bash
cd backend
mvn spring-boot:run
```

本地密钥和连接参数放在 `.env` 中：

```bash
cp .env.example .env
```

然后编辑 `.env`，填写 `APP_AI_API_KEY`。`.env` 已被 `.gitignore` 忽略，不要提交到仓库。

AI 中转站默认配置：

```text
APP_AI_BASE_URL=https://api.xxxxx.com
APP_AI_MODEL=gpt-5.5
APP_AI_API_STYLE=RESPONSES
```

也可以在 `ops_model_config` 中配置默认模型；未配置 API Key 时会继续读取 `APP_AI_API_KEY`。

启动前端：

```bash
cd frontend
npm install
npm run dev
```

默认地址：

- 后端健康检查：http://localhost:8080/api/health
- 前端开发服务：http://localhost:5173

初始化账号：

```text
用户名：admin
密码：admin123456
```

后端首次连接数据库时会通过 Flyway 执行 `backend/src/main/resources/db/migration/V1__init_schema.sql`，创建基础表、角色权限、风险规则和初始管理员账号。

## 开源协议

本项目基于 Apache License 2.0 开源，详见 [LICENSE](./LICENSE)。
