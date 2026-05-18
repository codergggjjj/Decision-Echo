# 项目 AI 交接文档

## 1. 项目概况

项目名称：个人决策回测器 / Decision Echo

项目目标：帮助用户记录当下的重要或日常决策，包括决策背景、候选方案、最终选择、选择理由、回看时间。到期后用户可以补充回测结果，用于复盘自己的判断质量。

当前项目是 Spring Boot + Vue 3 的前后端分离项目，已实现登录、注册、修改密码、首页 Dashboard、决策记录、新建决策、条件查询、补回测等核心功能。

开发时请优先保持现有业务结构和 UI 风格，不要大范围重构。

## 2. 技术栈与目录结构

根目录：

```text
D:\desktop\store\Exam_final
```

主要目录：

```text
Exam_final/
├─ backed/          # 后端 Spring Boot 项目
├─ fronted/         # 前端 Vue 3 项目
├─ sql/             # 可能存在 SQL 脚本
├─ test_data/       # 测试数据、验证资料
└─ 2026-05-14-个人决策回测器-SpringBoot-Vue版设计.md
```

后端技术栈：

```text
Spring Boot
MyBatis 注解 Mapper
MySQL
BCrypt 密码加密
JUnit 5
Maven Wrapper
```

前端技术栈：

```text
Vue 3
Vite
Pinia
Vue Router
Element Plus
Axios
原生 CSS
```

默认服务端口：

```text
后端：http://localhost:9889
前端：http://localhost:5173
MySQL：localhost:3306
数据库：exam_final
用户名：root
密码：123123
```

注意：历史上曾误写过 MySQL 为 `localhost:8080`，实际联调优先使用 `3306`。

## 3. 当前已完成功能

已完成核心功能：

```text
1. 登录
2. 注册
3. 登录后修改密码
4. 数字图片验证码
5. token 登录状态保存
6. 首页 Dashboard 数据展示
7. 新建决策记录
8. 候选方案树状录入
9. 标记最终选择方案
10. 过去记录展示
11. 待回看记录展示
12. 回测结果统计
13. 补回测
14. 决策条件查询
15. 前后端基本联调
```

登录模块当前状态：

```text
登录页支持“登录 / 注册”分段切换。
登录需要用户名、密码、数字验证码。
注册需要用户名、昵称、密码、确认密码、数字验证码。
注册成功后直接保存 token 和用户信息，并跳转 Dashboard。
首页右上角已有“修改密码”入口。
修改密码成功后会清除本地登录状态并跳回登录页。
```

首页 / Dashboard 当前状态：

```text
首页以用户过去记录为第一优先展示内容。
顶部显示用户昵称、问候语、刷新、修改密码、退出。
左侧展示过去决策记录流。
右侧展示待回看和回测结果统计。
底部或浮动按钮提供“记录新决定”。
新建决策使用居中 Modal/Dialog，不使用右侧抽屉。
补回测使用美化后的 Dialog。
```

决策记录和回测当前状态：

```text
用户可以创建决策。
决策包含标题、背景、标签、心情、候选方案、最终选择、原因、紧急度、回看时间。
背景允许为空。
标签和心情使用预设下拉。
候选方案使用两级树状结构。
回看时间到期且 status=pending 的记录会进入待回看。
补回测后 status 改为 reviewed，并记录满意度和反馈。
```

## 4. 后端当前状态

后端架构要求：

```text
controller / service / mapper 分层
不要把业务逻辑写进 controller
Mapper 使用 MyBatis 注解 SQL
Service 负责校验、事务和业务规则
```

主要包结构大致如下：

```text
backed/src/main/java/com/exam/exam_backed/
├─ auth/
│  ├─ controller/
│  ├─ dto/
│  ├─ service/
│  └─ vo/
├─ decision/
│  ├─ controller/
│  ├─ dto/
│  ├─ mapper/
│  ├─ service/
│  └─ vo/
├─ user/
│  └─ mapper/
└─ common/
```

认证模块当前接口：

```text
GET  /api/auth/captcha
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/me
PUT  /api/auth/password
POST /api/auth/logout
```

认证规则：

```text
用户名：4-50 位，只允许字母、数字、下划线
昵称：2-20 位
密码：8-32 位，至少包含字母和数字
注册必须校验验证码
登录必须校验验证码
修改密码必须登录
修改密码不需要验证码
修改密码必须校验旧密码
新密码不能和旧密码相同
确认密码必须一致
密码使用 BCrypt 存储
```

决策模块当前接口：

```text
GET  /api/decisions/dashboard
GET  /api/decisions
POST /api/decisions
PUT  /api/decisions/{id}/review
```

`GET /api/decisions` 查询参数：

```text
keyword：按决策标题模糊查询
tag：按标签包含查询
status：pending / reviewed / 空
limit：默认 50，最大 100
```

Dashboard 聚合逻辑：

```text
summary.total：当前用户全部决策数
summary.pending：当前用户 pending 数
summary.reviewed：当前用户 reviewed 数
summary.satisfaction：按 满意 / 一般 / 后悔 聚合，缺失补 0
recent：按 create_time DESC, id DESC 返回最近记录
pendingReview：status=pending 且 review_time <= NOW()
```

## 5. 前端当前状态

前端主要目录：

```text
fronted/src/
├─ api/
│  ├─ auth.js
│  └─ decision.js
├─ constants/
│  └─ apis.js
├─ router/
├─ store/
│  └─ auth.js
├─ utils/
│  ├─ request.js
│  └─ token.js
├─ views/
│  ├─ login/LoginView.vue
│  └─ dashboard/DashboardView.vue
└─ styles/index.css
```

前端认证状态：

```text
Pinia auth store 管理 token 和 user。
token 存本地。
Axios 请求拦截器自动带 Authorization: Bearer token。
401 时清理 token 和 user。
```

登录页状态：

```text
LoginView.vue 已实现登录 / 注册切换。
验证码图片从后端接口获取。
登录成功进入 /dashboard。
注册成功进入 /dashboard。
```

Dashboard 状态：

```text
DashboardView.vue 负责首页主体验。
包含：
- 顶部用户信息和操作区
- 统计卡片
- 决策搜索栏
- 过去记录流
- 待回看列表
- 回测结果统计
- 新建决策 Modal
- 补回测 Dialog
- 修改密码 Dialog
```

前端 API 常量：

```text
captcha: /api/auth/captcha
login: /api/auth/login
register: /api/auth/register
me: /api/auth/me
changePassword: /api/auth/password
logout: /api/auth/logout
decisionDashboard: /api/decisions/dashboard
decisions: /api/decisions
```

## 6. UI 风格偏好

整体风格：

```text
年轻化
扁平化
偏大众用户
不要管理后台感
参考 B 站风格
浅色背景
粉蓝强调色
8px 圆角
柔和阴影
轻动态 hover/focus
```

页面偏好：

```text
首页优先展示过去记录、回测和待回看。
新建、操作类内容应使用 Modal/Dialog/表单，不要占据主内容。
不要做商业 SaaS 风 landing page。
不要过度卡片化。
不要太“后台管理系统”。
不要深色、厚重、商务风。
```

表单偏好：

```text
字段有清晰 label。
placeholder 具体。
按钮有 loading 状态。
移动端触控高度不低于 44px。
输入框 focus 状态明显。
错误提示靠近字段。
```

新建决策表单现状：

```text
使用 el-dialog。
分组：基础信息 / 候选方案 / 选择原因与回看。
标签下拉：学习、消费、工作、生活、健康。
心情下拉：平静、焦虑、纠结、兴奋、冲动。
背景非必填。
候选方案为两级结构。
最终选择只能选顶层候选方案。
```

候选方案 JSON 格式：

```json
{
  "version": 1,
  "selectedId": "string",
  "items": [
    {
      "id": "string",
      "title": "方案标题",
      "children": [
        {
          "id": "string",
          "title": "补充点"
        }
      ]
    }
  ]
}
```

旧数据兼容：

```text
旧 options 如果是逗号分隔字符串，前端应兼容展示。
```

## 7. 接口与数据约定

用户表大致字段：

```text
user
- id
- username
- password_hash
- nickname
- status
- create_time
- update_time
```

决策表大致字段：

```text
decision
- id
- user_id
- title
- context
- options
- reason
- tags
- mood
- urgency
- review_time
- status
- satisfaction
- feedback
- create_time
- update_time
```

状态约定：

```text
pending：未回测
reviewed：已回测
```

满意度约定：

```text
满意
一般
后悔
```

标签约定：

```text
学习
消费
工作
生活
健康
```

心情约定：

```text
平静
焦虑
纠结
兴奋
冲动
```

默认测试账号：

```text
test_user / Test@123456
```

测试数据位置：

```text
/test_data/login_test_data.md
backed/sql/data.sql
```

SQL 目录要求：

```text
涉及表结构或测试数据 SQL 时，放在 backed/sql 目录下。
```

## 8. 后续开发原则

必须遵守：

```text
1. 后端按 controller / service / mapper 分层。
2. 前端保持 Vue 3 + Element Plus + Pinia 现有模式。
3. 不要随意引入新框架。
4. 不要重写整个页面，除非明确要求。
5. 不要破坏已有接口路径。
6. 不要移除已有功能。
7. 不要把 mock 数据替代真实接口。
8. 修改时优先小范围 patch。
9. 保持中文 UI 文案正常，不要引入乱码。
10. 遇到已有未提交改动，不要 revert。
```

文件操作限制：

```text
禁止批量删除文件或目录。
需要删除文件时，只能一次删除一个明确路径的文件。
如果需要批量删除文件，应停止操作，并让用户手动删除。
```

Git 限制：

```text
不要执行 git reset --hard。
不要 checkout 覆盖用户改动。
除非用户明确要求，否则不要提交或推送。
```

前端修改原则：

```text
优先改现有组件。
不要做大范围视觉重构。
不要添加无意义 hero 或营销页。
不要使用过度渐变、深色商务风、后台管理风。
Dashboard 应像面向普通年轻用户的个人工具。
```

后端修改原则：

```text
Service 负责业务校验。
Controller 只做参数接收和结果返回。
Mapper 只写 SQL。
涉及写操作需要考虑事务。
所有用户数据必须按 userId 隔离。
```

## 9. 默认验证限制

默认验证命令：

后端：

```bash
cd backed
.\mvnw.cmd test
```

前端：

```bash
cd fronted
npm run build
```

可选局部验证：

```bash
cd backed
.\mvnw.cmd -Dtest=AuthServiceImplTest test
```

启动项目：

```bash
cd backed
.\mvnw.cmd spring-boot:run
```

```bash
cd fronted
npm run dev -- --port 5173
```

默认不做：

```text
默认不要启动浏览器截图。
默认不要做完整人工验证码登录流程。
默认不要进行 Git 提交和推送。
默认不要跑与当前任务无关的大量探索命令。
默认不要新建复杂 E2E 测试。
默认不要批量删除文件。
```

什么时候需要浏览器验证：

```text
只有当前端布局、交互、弹窗、响应式明显改动时，再打开浏览器检查。
如果只是后端接口或小范围逻辑修改，优先跑单测和 build。
```

Vite 构建注意：

```text
当前 npm run build 可能出现大 chunk 警告或依赖 PURE 注释警告。
只要 build 成功，这些警告暂不作为阻塞问题。
```

## 10. 新对话使用模板

### 通用开发模板

```text
这是我的项目交接上下文，请按此继续开发，不要重新猜测项目结构。

项目路径：D:\desktop\store\Exam_final

项目是 Spring Boot + Vue 3 前后端分离：
- 后端目录：backed
- 前端目录：fronted
- 后端端口：http://localhost:9889
- 前端端口：http://localhost:5173
- MySQL：localhost:3306，root / 123123，数据库 exam_final

开发要求：
1. 后端必须按 controller / service / mapper 分层。
2. 前端保持 Vue 3 + Element Plus + Pinia 现有风格。
3. UI 保持年轻化、扁平化、浅色、粉蓝、B 站感，不要管理后台风。
4. 不要大范围重构，不要破坏已有接口。
5. 不要 revert 我的已有改动。
6. 禁止批量删除文件或目录。
7. 默认只跑：
   - backed: .\mvnw.cmd test
   - fronted: npm run build
8. 不需要浏览器截图、不需要 Git 提交，除非我明确要求。

当前已完成：
- 登录、注册、修改密码
- 验证码
- token 登录状态
- Dashboard 首页
- 决策新建
- 决策条件查询
- 候选方案树
- 最终选择展示
- 待回看
- 补回测
- 回测统计

现在我要你实现：
【在这里写新需求】
```

### 最小改动模板

```text
请在当前项目中做最小改动实现以下功能：
【功能描述】

限制：
1. 不要重构无关页面。
2. 不要改已有接口路径，除非必须。
3. 后端按 controller / service / mapper。
4. 前端沿用现有 UI 风格。
5. 只运行必要测试：
   - 后端相关单测或 .\mvnw.cmd test
   - 前端 npm run build
6. 不启动项目、不截图、不提交 Git。
```

### 只做计划模板

```text
先不要修改代码，只基于当前项目状态输出实现计划。

计划需要包含：
1. 涉及文件
2. 后端改动
3. 前端改动
4. 数据结构或 SQL 是否变化
5. 验证方式
6. 风险点
```

### 后端功能模板

```text
请只修改后端，实现：
【后端功能】

要求：
1. controller / service / mapper 分层。
2. Service 做业务校验。
3. Mapper 使用 MyBatis 注解 SQL。
4. 用户数据必须按 userId 隔离。
5. 补充或更新相关单元测试。
6. 验证命令：cd backed && .\mvnw.cmd test
```

### 前端功能模板

```text
请只修改前端，实现：
【前端功能】

要求：
1. 使用 Vue 3 + Element Plus + Pinia 现有结构。
2. UI 保持年轻化、浅色、粉蓝、8px 圆角、轻动态。
3. 不要做管理后台风。
4. 不要重构无关页面。
5. 移动端不能横向滚动，按钮触控高度不低于 44px。
6. 验证命令：cd fronted && npm run build
```
