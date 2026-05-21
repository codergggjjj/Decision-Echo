# PROJECT_HANDOFF.md

## 项目现状

Decision Echo 是个人决策回测器，采用前后端分离：

- 后端：`backed`，Spring Boot + MyBatis + MySQL。
- 前端：`fronted`，Vue 3 + Vite + Pinia + Element Plus。
- 已有能力：登录、注册、验证码、修改密码、个人中心、Dashboard、新建决策、条件查询、查看详情、软删除、补回测。

## 后端结构

后端根包：`backed/src/main/java/com/exam/exam_backed`。

- `auth`：登录、注册、验证码、Token 会话、密码修改。
- `user`：当前用户资料查询和更新。
- `decision`：决策创建、列表查询、详情、软删除、Dashboard 聚合、回测。
- `common`：统一响应、错误码、业务异常、全局异常处理。
- `config`：跨域等基础配置。

业务模块遵守 `controller / service / mapper / dto / vo` 分层。所有决策相关读写必须带 `userId` 条件，避免跨用户访问。

## 前端结构

前端根目录：`fronted/src`。

- `api`：接口请求封装。
- `constants/apis.js`：接口路径常量。
- `store/auth.js`：登录态、Token、当前用户信息。
- `router`：`/login`、`/dashboard`、`/profile` 与登录守卫。
- `views/login`：登录和注册。
- `views/dashboard`：首页、统计、搜索、记录流、新建/详情/回测 Dialog。
- `views/profile`：个人中心和资料编辑。
- `styles/index.css`：全局页面与组件样式。

## 当前接口

认证与用户：

- `GET /api/auth/captcha`
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`
- `PUT /api/auth/password`
- `POST /api/auth/logout`

决策：

- `GET /api/decisions/dashboard`
- `GET /api/decisions`
- `GET /api/decisions/{id}`
- `POST /api/decisions`
- `PUT /api/decisions/{id}/review`
- `DELETE /api/decisions/{id}`

## 数据结构要点

核心表：

- `user`：账号、密码哈希、昵称、头像、状态、创建时间。
- `decision`：标题、背景、候选方案、选择原因、标签、心情、紧急度、回测时间、满意度、反馈、状态、软删除标记。

`decision.options` 当前支持两种格式：

- 新格式：JSON，包含 `version`、`selectedId`、`items`，前端用于展示候选方案树和最终选择。
- 旧格式：逗号分隔字符串，前端和详情接口需要兼容展示。

`decision.status` 只表示回测状态：`pending` / `reviewed`。删除状态使用 `deleted` 字段，不混入 `status`。

## UI 偏好

- 年轻化、浅色、扁平、个人应用风格。
- 避免企业后台感、深色厚重、过度营销 Hero。
- Dashboard 以过去记录、待回看、回测结果和统计为主。
- 新建、详情、回测、修改资料优先使用 Dialog/Modal。
- 移动端不能出现横向滚动。

## 默认验证

- 前端改动：`cd fronted && npm run build`。
- 后端改动：优先跑相关测试类，例如 `cd backed && .\mvnw.cmd -Dtest=DecisionServiceImplTest test`。
- 不默认启动服务、截图、跑 E2E 或全量测试，除非用户明确要求。
