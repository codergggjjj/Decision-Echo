# 个人决策回测器 (SpringBoot+Vue版) 期末设计文档

## 1. 项目定位与目标
- **项目名称**：个人决策回测器
- **项目性质**：期末大作业 / 课程设计
- **核心目标**：实现一个全栈Web应用，帮助用户记录决策、在特定时间回测满意度，并利用图表和AI提供复盘分析。
- **架构特点**：采用经典的“前后端分离+单体架构”，结构清晰、易于答辩讲解，且高度契合AI代码生成（如Cursor/Claude）的模式。

## 2. 技术栈与架构规范

### 2.1 核心技术栈

- **前端**：Vue 3 (Composition API) + Vite + Element Plus + ECharts + Pinia + Vue Router
- **后端**：Spring Boot 3.x + Maven (单模块)
- **持久层**：MyBatis-Plus (极大简化单表CRUD操作)
- **数据库**：MySQL 8.0
- **权限认证**：Sa-Token (轻量级，适合期末项目，摒弃繁重的Spring Security)

### 2.2 “AI 友好型”开发规范
为了让AI能高质量、一次性生成可用代码，项目遵循以下规范：
1. **统一返回格式**：后端所有API均返回 `Result<T>` 对象（包含 `code, message, data`）。
2. **全局异常处理**：使用 `@RestControllerAdvice` 统一拦截异常，避免业务代码中出现冗余的 try-catch。
3. **极简实体类**：后端实体类（Entity, DTO, VO）统一使用 Lombok 的 `@Data` 注解。
4. **软删除与自动填充**：利用 MyBatis-Plus 实现 `create_time` 和 `update_time` 自动填充。

### 2.3 总体框架架构设计
参考老师提供的 `template` 框架，本项目在保留 Spring Boot + Vue 技术选型的基础上，将其思想迁移为更适合 Java 后端和 Vue 前端的工程结构。整体采用“前后端分离 + 后端分层 MVC + 前端模块化页面”的架构。

```text
浏览器 / 用户
   │
   ▼
Vue 3 前端应用
   ├── 路由层：页面跳转、登录拦截、404/403/500 页面
   ├── 视图层：登录、决策列表、决策详情、回测表单、分析看板
   ├── 组件层：表格、侧边栏、图表卡片、表单组件
   ├── 状态层：Pinia 保存用户信息、Token、页面状态
   └── 请求层：Axios 统一封装接口、错误提示、Token 注入
   │
   ▼
Spring Boot 后端服务
   ├── Controller：接收前端请求，完成参数校验和接口路由
   ├── Service：处理核心业务逻辑，如决策创建、回测、统计分析
   ├── Mapper：通过 MyBatis-Plus 访问数据库
   ├── Interceptor：登录态校验、权限拦截
   ├── Exception：统一异常处理
   ├── Common：统一响应体、错误码、常量
   └── Config：跨域、Sa-Token、MyBatis-Plus、AI 接口配置
   │
   ▼
MySQL 数据库
   ├── user：用户信息
   └── decision：决策记录与回测结果
```

### 2.4 与老师推荐框架的对应优化
老师模板中强调了 `middleware`、`routes`、`utils`、`constants`、`scripts`、`docs` 等工程化目录。本项目虽然使用 Spring Boot 和 Vue，不直接照搬 Node.js + React 目录，但会保留其设计思想：

| 老师模板目录 | 本项目对应设计 | 作用说明 |
| --- | --- | --- |
| `backend/middleware/errorHandler.js` | `backend/src/main/java/com/decision/exception/GlobalExceptionHandler.java` | 统一异常处理，保证接口错误格式一致 |
| `backend/middleware/validator.js` | `controller` 参数校验 + `jakarta.validation` 注解 | 对注册、登录、创建决策等入参进行合法性校验 |
| `backend/routes/health.js` | `HealthController` / `/api/health` | 提供后端健康检查接口，方便验收和部署测试 |
| `backend/utils/response.js` | `common/Result.java` | 统一成功、失败、分页等返回结构 |
| `backend/constants/errorCodes.js` | `common/ErrorCode.java` | 统一维护错误码，避免魔法数字和提示文案散落 |
| `backend/scripts/seed.js` | `sql/data.sql` 或 `scripts/init-data.sql` | 初始化测试账号和演示数据 |
| `backend/.env.example` | `application-example.yml` | 提供数据库、Token、AI Key 等配置示例 |
| `frontend/src/app/*` | `frontend/src/views/*` | 按页面组织登录、看板、决策列表、错误页 |
| `frontend/src/component/*` | `frontend/src/components/*` | 沉淀可复用组件，如侧边栏、图表卡片、决策表格 |
| `frontend/src/constant/apis.js` | `frontend/src/constants/apis.js` | 统一维护接口路径 |
| `frontend/src/hooks/useRequest.js` | `frontend/src/hooks/useRequest.js` 或 `utils/request.js` | 封装请求状态、错误提示和加载效果 |
| `frontend/src/util/token.js` | `frontend/src/utils/token.js` | 统一处理 Token 存取 |
| `docs/error-codes.md` | `docs/error-codes.md` | 记录错误码，便于答辩说明系统规范性 |

### 2.5 分层职责说明
为了让代码结构更清晰，项目按职责拆分为以下层次：

1. **表现层（View / Controller）**：前端页面负责用户交互，后端 Controller 负责暴露 RESTful API。
2. **业务层（Service）**：封装项目核心业务，例如“创建决策”“提交回测”“生成分析数据”“拼接 AI 建议 Prompt”。
3. **数据访问层（Mapper）**：只负责数据库增删改查，不直接处理业务判断。
4. **通用基础层（Common / Config / Exception）**：负责统一响应、错误码、异常处理、跨域、鉴权等全局能力。
5. **文档与脚本层（docs / sql / scripts）**：负责数据库初始化、错误码说明、接口说明和部署说明，提高项目完整度。

## 3. 数据库设计 (MySQL)
采用精简的两张核心表设计，满足业务需求且方便答辩。

### 3.1 用户表 (`user`)
| 字段名 | 数据类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键，自增或雪花算法 |
| `username` | VARCHAR(50) | 用户名（唯一） |
| `password_hash` | VARCHAR(255) | 密码哈希值 |
| `create_time` | DATETIME | 注册时间 |

### 3.2 决策记录表 (`decision`)
| 字段名 | 数据类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `user_id` | BIGINT | 关联用户ID |
| `title` | VARCHAR(100) | 决策标题 |
| `context` | TEXT | 决策背景/场景 |
| `options` | JSON | 候选方案列表 (存储为JSON数组) |
| `reason` | TEXT | 做出该决策的核心原因 |
| `tags` | VARCHAR(255) | 标签 (如：职业, 消费, 感情，逗号分隔) |
| `mood` | VARCHAR(50) | 做决策时的情绪 (平静/焦虑/冲动等) |
| `urgency` | INT | 紧急度 (1:低, 2:中, 3:高) |
| `review_time` | DATETIME | 计划回测复盘的时间 |
| `satisfaction` | VARCHAR(20) | 回测满意度 (满意/一般/后悔)，初始为空 |
| `feedback` | TEXT | 回测时的文字复盘 |
| `create_time` | DATETIME | 创建时间 |
| `update_time` | DATETIME | 更新时间 |

## 4. 核心功能与 API 设计

### 4.1 认证模块 (Auth)
基于 Sa-Token 实现鉴权，Token 放在 HTTP Header 中。
- `POST /api/auth/register`：用户注册
- `POST /api/auth/login`：用户登录（调用 `StpUtil.login(id)` 并返回 token）
- `GET /api/auth/info`：获取当前登录用户信息
- `POST /api/auth/logout`：退出登录

### 4.2 决策记录模块 (Decision)
- `POST /api/decision`：创建新决策
- `GET /api/decision/page`：分页条件查询（支持按 `title` 模糊查询，按 `tags`、`urgency` 筛选）
- `GET /api/decision/{id}`：获取决策详情
- `PUT /api/decision/{id}/review`：提交回测结果（更新 `satisfaction` 和 `feedback`）
- `DELETE /api/decision/{id}`：删除决策

### 4.3 数据可视化分析 (Analysis - 高分亮点)
为前端 ECharts 提供数据支撑：
- `GET /api/analysis/satisfaction-pie`：统计当前用户的满意度分布（饼图数据）
- `GET /api/analysis/trend-line`：按月统计决策数量趋势（折线图数据）

### 4.4 AI 建议模块 (AI Advice - 高分亮点)
- `POST /api/advice/generate/{decisionId}`：
  - **逻辑**：后端查询该笔决策详情及用户的历史满意度分布，拼接为大模型 Prompt。
  - **调用**：后端通过 HTTP 客户端（如 Hutool、OkHttp 或 WebClient）调用第三方大模型API（如 智谱GLM / 阿里云百炼 等）。
  - **返回**：返回一段结构化的建议字符串，前端解析渲染。

## 5. 项目目录结构
参考老师模板项目，适配前后端分离的 SpringBoot + Vue 架构。目录设计重点不是简单堆文件，而是让“页面、组件、接口、状态、工具、错误码、初始化脚本”各自归位，降低开发和答辩理解成本。

```text
decision-tracker/
├── .github/                        # GitHub Actions 自动构建配置，可选
├── frontend/                       # 前端（Vue 3 + Vite）
│   ├── public/                     # 静态资源
│   │   └── logo.svg                # 项目 Logo
│   ├── src/
│   │   ├── api/                    # API 请求函数，按业务模块拆分
│   │   │   ├── auth.js             # 登录、注册、用户信息接口
│   │   │   ├── decision.js         # 决策记录增删改查接口
│   │   │   ├── analysis.js         # 图表统计接口
│   │   │   └── advice.js           # AI 建议接口
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 通用组件
│   │   │   ├── AppSidebar/         # 侧边栏菜单
│   │   │   ├── DecisionTable/      # 决策列表表格
│   │   │   ├── DecisionForm/       # 决策创建/编辑表单
│   │   │   └── ChartPanel/         # 图表容器组件
│   │   ├── constants/              # 常量配置
│   │   │   ├── apis.js             # 接口路径常量
│   │   │   ├── urls.js             # 前端路由地址常量
│   │   │   └── errorCodes.js       # 前端错误码映射
│   │   ├── hooks/                  # 组合式逻辑封装
│   │   │   ├── useAuth.js          # 登录状态与权限判断
│   │   │   └── useRequest.js       # 请求 loading/error 封装
│   │   ├── router/                 # 路由配置
│   │   ├── store/                  # Pinia 状态管理，对应老师 app/store 思想
│   │   │   └── auth.js             # 登录用户与 Token 状态
│   │   ├── utils/                  # 工具函数
│   │   │   ├── request.js          # Axios 实例、拦截器、统一错误处理
│   │   │   └── token.js            # Token 存取工具
│   │   ├── views/                  # 页面组件，参考老师 app 目录思想
│   │   │   ├── login/              # 登录/注册页
│   │   │   ├── dashboard/          # 数据分析看板
│   │   │   ├── decision-list/      # 决策列表页
│   │   │   ├── decision-detail/    # 决策详情与回测页
│   │   │   ├── forbidden/          # 403 页面
│   │   │   ├── not-found/          # 404 页面
│   │   │   └── server-error/       # 500 页面
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 前端入口文件
│   ├── .env                        # 前端环境变量
│   ├── .env.example                # 前端环境变量示例
│   ├── package.json                # 前端依赖配置
│   ├── eslint.config.js            # 代码规范配置
│   └── vite.config.js              # Vite 配置文件
│
├── backend/                        # 后端（Spring Boot 3.x）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/decision/
│   │   │   │   ├── config/         # 基础配置 (CORS, Sa-Token, MyBatis-Plus, AI Client)
│   │   │   │   ├── controller/     # 控制器层 (API 路由)
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── DecisionController.java
│   │   │   │   │   ├── AnalysisController.java
│   │   │   │   │   ├── AdviceController.java
│   │   │   │   │   └── HealthController.java
│   │   │   │   ├── entity/         # 数据库实体类
│   │   │   │   ├── dto/            # 请求参数对象，如 LoginDTO、DecisionCreateDTO
│   │   │   │   ├── vo/             # 响应视图对象，如 UserVO、DecisionDetailVO
│   │   │   │   ├── mapper/         # 数据库操作层
│   │   │   │   ├── service/        # 业务接口
│   │   │   │   ├── service/impl/   # 业务实现类
│   │   │   │   ├── interceptor/    # 权限拦截器配置
│   │   │   │   ├── exception/      # 统一异常处理，对应老师 middleware/errorHandler
│   │   │   │   ├── common/         # 统一响应、错误码、分页对象
│   │   │   │   │   ├── Result.java
│   │   │   │   │   ├── ErrorCode.java
│   │   │   │   │   └── PageResult.java
│   │   │   │   └── constants/      # 系统常量，如满意度、紧急度、Token Header
│   │   │   └── resources/
│   │   │       ├── application.yml # 配置文件
│   │   │       └── application-example.yml # 配置示例，隐藏真实密码和 AI Key
│   └── pom.xml                     # Maven 依赖配置
│
├── sql/                            # 数据库脚本
│   ├── schema.sql                  # 基础建表脚本
│   └── data.sql                    # 预置测试数据脚本
│
├── scripts/                        # 项目辅助脚本
│   └── init-data.sql               # 可选：初始化演示账号和样例决策
│
├── docs/                           # 项目文档
│   ├── api.md                      # 接口说明
│   ├── error-codes.md              # 错误码说明
│   └── deploy.md                   # 部署说明
│
├── docker-compose.yml              # 容器化一键部署编排配置
├── .gitignore                      # Git 忽略配置
├── .prettierrc                     # 前端格式化配置
└── README.md                       # 项目说明与部署指南
```

### 5.1 后端框架架构优化说明
后端采用 Spring Boot 的标准分层架构，并吸收老师模板中“中间件、统一响应、错误码、初始化脚本”的思想。

1. **Controller 层只负责接口入口**：不直接写复杂业务逻辑，只接收参数、调用 Service、返回 `Result`。
2. **Service 层负责业务闭环**：例如创建决策时校验用户登录态、保存记录；提交回测时校验记录归属；生成 AI 建议时组合历史数据。
3. **Mapper 层保持轻量**：基于 MyBatis-Plus 完成单表操作，复杂统计查询单独写 XML 或 Wrapper。
4. **Exception 层统一兜底**：所有业务异常抛出 `BusinessException`，最终由 `GlobalExceptionHandler` 转成统一错误响应。
5. **Common 层沉淀规范**：`Result`、`ErrorCode`、`PageResult` 等类保证接口风格一致，便于前端统一处理。
6. **Health 接口提高完整度**：提供 `/api/health`，返回服务状态、当前时间、数据库连接状态，方便老师快速验收。

### 5.2 前端框架架构优化说明
前端在 Vue 3 中参考老师模板的 `app/component/constant/hooks/util` 思路，将页面、组件、接口和工具拆开。

1. **views 对应页面路由**：每个页面是一个业务场景，如登录、看板、列表、详情、错误页。
2. **components 存放可复用组件**：例如 `DecisionTable`、`DecisionForm`、`ChartPanel`，避免页面代码过长。
3. **api 统一管理请求函数**：页面不直接写 URL，而是调用 `authApi.login()`、`decisionApi.getPage()` 这类函数。
4. **constants 统一维护常量**：接口路径、路由路径、错误码、菜单项集中管理。
5. **hooks 抽取复用逻辑**：如 `useAuth` 管理登录态，`useRequest` 管理 loading 和错误提示。
6. **utils 处理基础工具**：如 Token 存储、Axios 拦截器、日期格式化。

### 5.3 推荐的核心文件职责

| 文件 | 职责 |
| --- | --- |
| `backend/common/Result.java` | 统一 API 返回格式，封装 `success` 和 `fail` 方法 |
| `backend/common/ErrorCode.java` | 定义错误码，如未登录、参数错误、数据不存在、无权限 |
| `backend/exception/BusinessException.java` | 主动抛出的业务异常 |
| `backend/exception/GlobalExceptionHandler.java` | 统一捕获异常并返回标准 JSON |
| `backend/controller/HealthController.java` | 健康检查接口 |
| `frontend/src/utils/request.js` | Axios 实例、Token 注入、响应拦截、错误提示 |
| `frontend/src/constants/apis.js` | 后端接口路径集中管理 |
| `frontend/src/hooks/useAuth.js` | 判断是否登录、退出登录、获取当前用户 |
| `docs/error-codes.md` | 说明错误码含义，体现工程规范 |

## 6. AI 辅助开发实施路线 (Copilot / Cursor 任务拆解)

项目开发可按照以下顺序给 AI 派发任务：
1. **项目骨架初始化**：先创建 `backend`、`frontend`、`sql`、`docs`、`scripts` 目录，保证整体结构与设计文档一致。
2. **数据库初始化**：发送建表 SQL，让 AI 生成 `schema.sql`、`data.sql`，并准备测试账号和演示决策数据。
3. **后端基础能力**：让 AI 编写 `Result`、`ErrorCode`、`BusinessException`、`GlobalExceptionHandler`、`HealthController`。
4. **后端业务代码**：利用 MyBatis-Plus 生成 Entity、Mapper、Service，再分模块编写 Auth、Decision、Analysis、Advice Controller。
5. **前端基础能力**：搭建 Vite + Vue 3 项目，生成 `request.js`、`token.js`、`apis.js`、`useAuth.js`。
6. **前端页面开发**：按 `views` 目录逐个生成登录页、决策列表页、决策详情页、回测表单页、分析看板页。
7. **联调与文档补全**：根据真实接口补充 `docs/api.md` 和 `docs/error-codes.md`，最后完善 README 的启动步骤。

## 7. 期末验收标准
1. **基础跑通**：前后端能成功启动，数据库连接正常。
2. **完整闭环**：可以完整演示“注册 -> 登录 -> 记下一笔决策 -> 等待后填写回测 -> 查看看板图表”。
3. **亮点展示**：ECharts 图表渲染正确，能够成功调用 AI 接口生成决策建议。
4. **代码整洁**：无明显报错，模块划分符合 MVC 规范。



**老师推荐的框架**

  template/
  ├── backend/
  │   ├── middleware/
  │   │   ├── errorHandler.js
  │   │   └── validator.js
  │   ├── routes/
  │   │   └── health.js
  │   ├── utils/
  │   │   └── response.js
  │   ├── constants/
  │   │   └── errorCodes.js
  │   ├── scripts/
  │   │   └── seed.js
  │   ├── db.js
  │   └── .env.example
  ├── frontend/
  │   ├── public/
  │   │   └── ans.svg
  │   ├── src/
  │   │   ├── app/
  │   │   │   ├── admin/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   ├── dashboard/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   ├── forbidden/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   ├── login/
  │   │   │   │   └── index.module.less
  │   │   │   ├── not-found/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   ├── server-error/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   └── store/
  │   │   │       └── auth.js
  │   │   ├── component/
  │   │   │   ├── ExampleTable/
  │   │   │   │   ├── index.jsx
  │   │   │   │   └── index.module.less
  │   │   │   └── Sidebar/
  │   │   │       └── index.module.less
  │   │   ├── constant/
  │   │   │   ├── apis.js
  │   │   │   └── urls.js
  │   │   ├── hooks/
  │   │   │   ├── useAuth.js
  │   │   │   └── useRequest.js
  │   │   ├── img/
  │   │   │   └── logo.svg
  │   │   ├── util/
  │   │   │   └── token.js
  │   │   └── index.less
  │   ├── .env
  │   ├── index.html
  │   ├── vite.config.js
  │   └── eslint.config.js
  ├── .gitignore
  ├── .prettierrc
  └── docs/
      └── error-codes.md
