# AGENTS.md

## 1. 项目概述
Decision Echo (个人决策回测器) 是一个前后端分离应用。后端（`backed`）基于 Spring Boot + MyBatis，前端（`fronted`）基于 Vue 3 + Vite + Pinia + Element Plus。AI 在本项目中必须遵循“极简修改、不越界、节约 Token”的核心准则，通过直接修改代码解决具体问题，避免重复性分析和过度重构。

## 2. 快速命令

- **前端构建**：`cd fronted && npm run build`
- **前端启动**：`cd fronted && npm run dev`
- **后端局部测试**：`cd backed && .\mvnw.cmd -Dtest=<ClassName> test`
- **后端全量测试**：`cd backed && .\mvnw.cmd test`

## 3. 后端架构
```text
backed/src/main/java/com/exam/exam_backed/
├── auth/        # 登录、注册、验证码、密码修改、Token 会话
├── common/      # Result、ErrorCode、BusinessException、全局异常处理
├── config/      # 跨域等 Spring 配置
├── decision/    # 决策记录、查询、详情、软删除、回测和 Dashboard 聚合
└── user/        # 用户资料查询与更新
```
每个业务模块优先保持 `controller / service / mapper / dto / vo` 分层：Controller 只接收参数并返回 `Result`，Service 负责业务校验、事务和规则，Mapper 使用 MyBatis 注解 SQL。所有用户数据必须按 `userId` 隔离；禁止随意修改已有登录逻辑和数据库结构。

## 4. 前端架构
**技术栈**：Vue 3 + Vite + Pinia + Element Plus
```text
fronted/src/
├── api/          # axios 请求函数，页面不得直接拼散落 URL
├── constants/    # API 路径常量
├── router/       # /login、/dashboard、/profile 路由和登录守卫
├── store/        # Pinia 登录态、Token、当前用户信息
├── styles/       # 全局 UI 样式
├── utils/        # request、token 等通用工具
└── views/        # login、dashboard、profile 页面
```
**UI 设计约束**：
- 风格：年轻化、扁平化、浅色系，柔和圆角与阴影（严禁企业后台/SaaS风）。
- 交互：新增、编辑、补回测操作一律使用 Modal/Dialog，不独占整个视图。
- 响应式：移动端严禁出现横向滚动条。

## 5. 关键约定
1. **最小化原则**：只改直接相关文件，绝对不自行扩展未要求的功能，绝对不进行大范围重构。
3. **安全底线**：禁止执行 `git reset --hard` / `git clean` 等破坏性命令，禁止随意 commit/push 或覆盖用户更改。
4. **简洁输出**：完成后只输出“改了什么、验证了什么、需用户处理什么”，不发长篇计划，不发完整代码 Diff。
5. **精准读取**：不要为“全面了解”去扫描全量文件，直接针对报错文件或需求相关模块读取。

## 6. 本地开发及验证流程
1. **修改前**：执行 `git status`，仅通过 `Read` 查阅当前任务直接相关的文件。
2. **修改中**：优先遵循已有代码风格和框架习惯。
3. **验证闭环**：
   - **前端**默认仅执行 `npm run build`，**禁止**私自起服务并在浏览器截图或跑复杂 E2E。
   - **后端**优先使用 `mvnw test -Dtest=...` 跑涉及类的单测。
4. 日志不贴回控制台：所有长篇测试日志和工具输出禁止粘贴在与用户的对话中。

## 7. 质量检查
| 检查类型 | 命令示例 | 使用场景 |
| --- | --- | --- |
| 前端 Build | `cd fronted && npm run build` | 前端代码变动后必须执行以保证无编译错误 |
| 后端局部单测 | `cd backed && .\mvnw.cmd -Dtest=AuthServiceImplTest test` | 后端某 Service/Controller 改动后 |
| 后端全量测试 | `cd backed && .\mvnw.cmd test` | 明确要求或改动波及全局多个模块时 |

## 8. 参考项目约定
项目沿用经典 Spring Boot Web 架构和主流的 Vue 3 Composition API 模式。接口及组件参考已有代码即可，无需引入外部重量级框架或复杂的第三方库。

## 9. 文档导航
| 文档 | 作用说明 |
| --- | --- |
| `architecture.md` | 架构文档 |
| `AGENTS.md` | AI 工作流与项目规约（每次必读，当前文件） |
| `PROJECT_HANDOFF.md` / `AI项目交接文档.md` | 项目现状、接口、数据结构、UI 偏好；只在需要背景时读取相关部分 |
