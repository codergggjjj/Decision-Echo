# 长期目标关联 (Long-term Goal Alignment) 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**目标：** 实现“长期目标关联”功能。允许用户创建长期目标，并在进行单次决策时将其与长期目标进行关联，从而获取更具长期视角的 AI 建议。同时引入独立的标签(Tag)系统，实现目标与决策的智能推荐关联。

**UI 设计与前端参考说明 (核心原则)：**
前端长期目标相关页面的布局、UI 交互、样式与视觉结构，**必须严格参考**本地外部目录 `D:\desktop\store\Exam_final\long-term-main-page-main` 中的 React 代码。
**绝对要求：** 当前项目必须保持 Vue 3 框架结构！执行任务时需读取该目录下的 React 组件和 CSS 文件，提取其设计精髓和样式，将其**完全翻译/复刻为 Vue 3 (Composition API)** 代码，绝不可引入 React 依赖或改变现有项目技术栈。

**架构设计指导：** 
1. **数据库层**：引入独立的 `goal` 表及标签系统 (`tag`, `goal_tag`, `decision_tag`)。在现有的 `decision` 表中保留 `goal_id` 用于唯一关联。
2. **后端层**：实现 Goal 与 Tag 的 CRUD；增加基于 Tag 匹配的智能推荐算法；修改 Decision 模块并调整 AI Advice 服务。
3. **前端层**：根据参考的 React 设计转化为 Vue 3，新增长期目标的列表与详情视图；调整新建决策表单支持智能推荐与手动选择；渲染目标契合度模块。

**技术栈：** Java, Spring Boot, MyBatis-Plus, Vue 3, Vue Router.

---

### Task 1: 数据库定义更新 (包含标签系统)

**相关文件：**
- 修改/创建：数据库初始化 SQL 脚本，或者直接在数据库中执行。

- [ ] **Step 1: 创建 `goal` 表**
  - 要求：表名 `goal`，字段需包含 `id` (主键自增), `user_id` (非空), `title`, `description`, `category`, `priority` (默认 'MEDIUM'), `status` (默认 'IN_PROGRESS'), `target_date`, `measurement`, `progress` (默认 0), `created_at`, `updated_at`。

- [ ] **Step 2: 创建标签系统相关表 (`tag`, `goal_tag`, `decision_tag`)**
  - `tag` 表：`id`, `user_id`, `name`, `created_at`, `updated_at`。(user_id与name加联合唯一索引)
  - `goal_tag` 表：`id`, `goal_id`, `tag_id`。(goal_id与tag_id加联合唯一索引)
  - `decision_tag` 表：`id`, `decision_id`, `tag_id`。(decision_id与tag_id加联合唯一索引)

- [ ] **Step 3: 修改 `decision` 表**
  - 要求：为 `decision` 表新增 `goal_id` (BIGINT) 字段，允许为空。保留旧的 `tags` 字段作历史兼容。

### Task 2: 后端 - Goal 实体与持久层

**相关文件：**
- 创建: `backed/src/main/java/com/exam/exam_backed/goal/Goal.java`
- 创建: `backed/src/main/java/com/exam/exam_backed/goal/mapper/GoalMapper.java`

- [ ] **Step 1: 定义 Goal 实体类**
  - 要求：配置 `@TableName("goal")`、`@TableId` 和自动填充策略 (`createdAt`, `updatedAt`)。

- [ ] **Step 2: 创建 GoalMapper 接口**
  - 要求：继承 `BaseMapper<Goal>`，加上 `@Mapper`。

### Task 3: 后端 - 标签系统 (Tag) 基础实现

**相关文件：**
- 创建: `Tag.java`, `GoalTag.java`, `DecisionTag.java` 及对应的 Mappers
- 创建: `TagService.java` 及 `TagServiceImpl.java`

- [ ] **Step 1: 定义标签相关实体与 Mapper**
  - 要求：分别为 `tag`, `goal_tag`, `decision_tag` 创建实体和 Mapper。

- [ ] **Step 2: 实现 TagServiceImpl**
  - 要求：提供 `saveTags(Long userId, List<String> tagNames)` 方法。如果标签已存在则返回，不存在则创建。
  - 提供绑定方法：`bindGoalTags(Long goalId, List<Long> tagIds)` 和 `bindDecisionTags(Long decisionId, List<Long> tagIds)`。

### Task 4: 后端 - Goal 业务逻辑层与智能推荐

**相关文件：**
- 创建: `GoalRequest.java`, `GoalService.java`, `GoalServiceImpl.java`

- [ ] **Step 1: 实现 Goal 基础 CRUD**
  - 要求：在 `GoalServiceImpl` 中实现目标创建/更新/详情查询，创建/更新目标时需调用 `TagService` 解析传入的字符串标签并与目标绑定。

- [ ] **Step 2: 实现智能推荐算法 `recommendGoalsByTags`**
  - 要求：在 `GoalService` 中增加方法，传入 `List<String> tagNames`，根据标签找出对应的 `tag_id`。
  - 在 `goal_tag` 表中统计匹配 `tag_id` 最多的前3个且状态为 `IN_PROGRESS` 的 `goal_id`，返回推荐目标列表。

### Task 5: 后端 - 控制层 (Controller) 暴露

**相关文件：**
- 创建: `GoalController.java`

- [ ] **Step 1: 实现 RESTful API**
  - 暴露 POST, GET 列表/详情, PUT, DELETE 接口。
  - 增加推荐接口：`POST /api/goals/recommend` 接收 `{ tags: ["学习"] }` 并返回推荐目标。

### Task 6: 后端 - 决策模块整合

**相关文件：**
- 修改: `Decision.java`, 各类 DTO, `DecisionServiceImpl.java`, `DecisionController.java`

- [ ] **Step 1: Decision 模型增加 goalId 与多标签处理**
  - 在实体与 DTO 中增加 `goalId`。在创建/更新时调用 `TagService` 写入 `decision_tag`。

- [ ] **Step 2: 提供基于目标的决策查询**
  - 增加 `getDecisionsByGoalId` 并在 `DecisionController` 暴露 `GET /api/decisions/goal/{goalId}`。

### Task 7: 后端 - AI 建议增强逻辑

**相关文件：**
- 修改: `AdviceServiceImpl.java`
- 修改: `DecisionAdviceResponse.java`

- [ ] **Step 1: 扩充返回结构**
  - 增加 `GoalAlignmentDTO` 包含契合度分析。

- [ ] **Step 2: 动态拼接 Prompt**
  - 在系统 Prompt 中注入目标上下文，要求 AI 输出 `goalAlignment` JSON 节点。

### Task 8: 前端 - 基础配置与参照 React 移植视图

**相关文件：**
- 参考目录: `D:\desktop\store\Exam_final\long-term-main-page-main`
- 创建: `fronted/src/api/goal.js`
- 修改: `fronted/src/router/index.js`, `fronted/src/components/AppShell.vue`
- 创建: `GoalListView.vue`, `GoalDetailView.vue`

- [ ] **Step 1: API 与路由配置**
  - 封装 RESTful 与推荐接口，配置路由。

- [ ] **Step 2: 提取 React 设计并用 Vue 3 重写**
  - **前置动作**：读取 `D:\desktop\store\Exam_final\long-term-main-page-main` 下的 React 组件源码和 CSS。
  - **GoalListView.vue 开发**：完全复刻 React 版本的页面布局、卡片样式、颜色与动画效果，使用 Vue 3 的 `<script setup>` 和对应的模板语法重写。
  - **GoalDetailView.vue 开发**：同上，复刻 React 设计中的详情页逻辑（包括进度条、统计展示等），底部加入调用 `/api/decisions/goal/{id}` 渲染的关联决策列表。

### Task 9: 前端 - 智能推荐与决策流程整合

**相关文件：**
- 修改: `DashboardView.vue` (或新建决策表单)
- 修改: `AnalysisView.vue` (或建议展示组件)

- [ ] **Step 1: 表单支持标签驱动推荐**
  - 用户输入标签后调用 `POST /api/goals/recommend`，展示“✨ 智能推荐”，支持一键选中。

- [ ] **Step 2: 展示 AI 契合度**
  - 渲染 AI 返回结果时展示 `goalAlignment` 模块。