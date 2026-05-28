# 后端长期目标与标签推荐系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Summary**
在现有 Spring Boot + MyBatis-Plus 后端中新增长期目标 Goal、标签 Tag、目标推荐接口，并把 goalId 与标签绑定接入现有决策和 AI 建议流程。保持当前项目分层风格、Result 包装、AuthSessionService.currentUserId() 获取当前用户；该方法底层已调用 StpUtil.getLoginIdAsLong()。SQL 追加到 backed/sql/schema.sql，后续由用户手动执行或重新初始化数据库。

**Key Changes**

**数据库脚本：**
- 在 `backed/sql/schema.sql` 追加 `goal`、`tag`、`goal_tag`、`decision_tag` 建表 SQL。
- 给 `decision` 追加可空 `goal_id BIGINT`，并加 `idx_decision_goal`。
- 保留 `decision.tags` 字段兼容旧数据；新增 `decision_tag` 只做结构化标签绑定。

**Goal 模块：**
- 新增 `goal` 包，包含实体、Mapper、DTO、VO、Service、Controller。
- `GET /api/goals?status=IN_PROGRESS` 返回当前用户目标列表，列表项包含前端需要的 id/title/description/status/priority/category/progress/targetDate/decisionCount。
- `GET /api/goals/{id}` 返回聚合详情：目标字段 + stats + decisions，匹配现有 GoalDetailView.vue。
- 实现 POST/PUT/DELETE 接口。
- 删除目标时只解除当前用户该目标关联的决策 goal_id = NULL，不删除决策。

**Tag 与推荐：**
- 新增 `tag` 包，包含 Tag、GoalTag、DecisionTag 实体/Mapper 和 TagService。
- `TagService.getOrCreateTags(userId, tagNames)` 去空、去重、按 (user_id, name) 复用或创建标签。
- `bindGoalTags`、`bindDecisionTags` 先清理旧绑定再写入新绑定。
- `POST /api/goals/recommend` 接收 { "tags": ["学习", "工作"] }，返回当前用户 IN_PROGRESS 且命中标签数最多的前 3 个目标。
- 新增逻辑全部使用 LambdaQueryWrapper / LambdaUpdateWrapper。

**决策与 AI 整合：**
- `Decision` 相关类增加 `goalId`，详情额外返回 `goalTitle`。
- `DecisionServiceImpl.create` 写入 goalId，校验目标属于当前用户；保存后将 request.tags 解析并写入 `decision_tag`。
- 新增 `GET /api/decisions/goal/{goalId}`，返回当前用户该目标下未删除决策。
- `AdviceGenerateRequest` 增加 goalId。
- `AdviceServiceImpl` 注入 GoalMapper，构建 prompt 时加入目标详细信息。
- `DecisionAdviceResponse` 增加 goalAlignment，内部包含 score/level/bestOption/reason/optionAnalysis；normalize 时保留或填空结构。

**API Contracts**
详见原说明，包括 GET 列表、GET 详情（包含 stats 和 decisions）、POST/PUT/DELETE 以及 POST 推荐接口。

**Test Plan**
- 新增 GoalServiceImplTest 验证 CRUD、隔离性和推荐逻辑。
- 更新 DecisionServiceImplTest 验证目标关联和用户隔离。
- 更新 AdviceServiceImplTest 验证 prompt 拼接和返回解析。
- 验证命令：
  - `cd backed && .\mvnw.cmd -Dtest=GoalServiceImplTest test`
  - `cd backed && .\mvnw.cmd -Dtest=DecisionServiceImplTest test`
  - `cd backed && .\mvnw.cmd -Dtest=AdviceServiceImplTest test`

**Assumptions**
- 后端实现时一口气完成，不按阶段暂停。
- SQL 直接更新 `backed/sql/schema.sql`，不引入 Flyway/Liquibase。
- Controller 继续复用项目现有 `AuthSessionService.currentUserId()`。
- 不重构现有逻辑，最小改动。

---

### Task 1: 数据库脚本更新

**Files:**
- Modify: `backed/sql/schema.sql` (If it doesn't exist, create it in `backed/src/main/resources/db/schema.sql` or similar standard path, plan assumes `backed/sql/schema.sql`)

- [ ] **Step 1: Append table definitions and alter statement**
Append the SQL for `goal`, `tag`, `goal_tag`, `decision_tag`, and the `ALTER TABLE decision` statement as described in the summary.

### Task 2: Tag 模块实现

**Files:**
- Create: Entities (`Tag`, `GoalTag`, `DecisionTag`)
- Create: Mappers (`TagMapper`, `GoalTagMapper`, `DecisionTagMapper`)
- Create: `TagService` & `TagServiceImpl`

- [ ] **Step 1: Implement Entities and Mappers**
- [ ] **Step 2: Implement `getOrCreateTags`, `bindGoalTags`, `bindDecisionTags` in TagServiceImpl**

### Task 3: Goal 模块基础 CRUD 与推荐

**Files:**
- Create: `Goal` entity, `GoalMapper`
- Create: DTOs/VOs (`GoalRequest`, `GoalListItemVO`, `GoalDetailVO`, `GoalStatsVO`)
- Create: `GoalService` & `GoalServiceImpl`

- [ ] **Step 1: Implement basic CRUD (create, update, delete)**
Ensure user isolation and tag binding during create/update. Delete should only set `goal_id = NULL` in `decision`.
- [ ] **Step 2: Implement list and detail retrieval**
List filters by status. Detail calculates stats (decisionCount, reviewedCount, etc.) and fetches basic decisions.
- [ ] **Step 3: Implement `recommendGoalsByTags`**
Use `TagService` to get tags, count matches in `goal_tag`, return top 3.

### Task 4: Goal Controller

**Files:**
- Create: `GoalController`

- [ ] **Step 1: Expose REST endpoints**
Expose GET list, GET detail, POST, PUT, DELETE, and POST recommend endpoints using `AuthSessionService.currentUserId()`.

### Task 5: 整合 Decision 模块

**Files:**
- Modify: `Decision`, `DecisionCreateRequest`, `DecisionDetail`
- Modify: `DecisionServiceImpl`, `DecisionController`

- [ ] **Step 1: Update Entities/DTOs to include `goalId`**
- [ ] **Step 2: Update `DecisionServiceImpl.create`**
Set `goalId`, verify ownership, bind tags via `TagService`.
- [ ] **Step 3: Implement and expose `getDecisionsByGoalId`**

### Task 6: 整合 AI 建议模块

**Files:**
- Modify: `AdviceGenerateRequest`, `DecisionAdviceResponse`, `AdviceServiceImpl`

- [ ] **Step 1: Update Response structure**
Add `goalAlignment` structure.
- [ ] **Step 2: Enhance Prompt generation**
Fetch `Goal` details if `goalId` is present and append to prompt. Ensure AI outputs `goalAlignment` JSON.

### Task 7: 编写并运行测试

**Files:**
- Create: `GoalServiceImplTest`
- Modify: `DecisionServiceImplTest`, `AdviceServiceImplTest`

- [ ] **Step 1: Implement and run GoalServiceImplTest**
- [ ] **Step 2: Update and run DecisionServiceImplTest**
- [ ] **Step 3: Update and run AdviceServiceImplTest**