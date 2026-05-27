# 长期目标关联 (Long-term Goal Alignment) 功能设计文档

## 1. 业务背景与价值
在原有的“单次决策与复盘”逻辑中，用户的决策是孤立的。引入“长期目标（Goal）”模块后，用户可以设定具有时间跨度的宏大目标（如“三年内实现财务自由”、“转行成为全栈工程师”）。
每次面临具体决策时，可以将其挂载到特定的长期目标上。系统通过 AI 在生成决策建议时，会主动评估当前选项是否与长期目标一致，从而帮助用户做出更具长远眼光的选择，实现决策的“长期主义”。

## 2. 核心功能实现清单

### 2.1 目标管理 (CRUD)
*   **新建目标**：用户可以创建长期目标，填写标题、描述、预期完成时间和当前状态（进行中/已完成/已放弃）。
*   **查看目标列表**：在一个专门的视图中查看所有设定的目标及其状态。
*   **修改/删除目标**：支持对目标的生命周期进行管理。

### 2.2 决策与目标的关联
*   **决策表单改造**：在创建或编辑决策（Decision）时，新增一个可选项，允许用户选择一个“进行中”的长期目标与之关联（非必填）。
*   **目标详情页**：在目标管理中，点击某个目标可以查看所有关联到该目标的“历史决策”列表。

### 2.3 AI 智能评估增强
*   **Prompt 注入**：当决策关联了目标时，后端在请求 AI 生成建议前，会查询该目标的详细描述。
*   **建议生成改造**：AI 会在原有的利弊分析基础上，增加一个维度：评估各个候选选项与“长期目标”的契合度，并明确指出哪个选项最有利于长期目标的达成。

## 3. 技术实现设计

### 3.1 数据库设计
**新增 `goal` 表**
```sql
CREATE TABLE goal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '目标ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    title VARCHAR(100) NOT NULL COMMENT '目标标题',
    description TEXT COMMENT '目标详细描述',
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS, COMPLETED, ABANDONED',
    target_date DATE COMMENT '预期达成日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户长期目标表';
```

**修改 `decision` 表**
```sql
ALTER TABLE decision ADD COLUMN goal_id BIGINT DEFAULT NULL COMMENT '关联的长期目标ID';
ALTER TABLE decision ADD CONSTRAINT fk_decision_goal FOREIGN KEY (goal_id) REFERENCES goal(id) ON DELETE SET NULL;
```

### 3.2 后端接口设计 (Controller/Service)
新增 `GoalController` 和 `GoalService`，提供以下接口：
*   `POST /api/goals` - 创建目标
*   `GET /api/goals` - 查询当前用户的目标列表
*   `GET /api/goals/{id}` - 获取目标详情（包含关联的决策列表）
*   `PUT /api/goals/{id}` - 更新目标信息
*   `DELETE /api/goals/{id}` - 删除目标

### 3.3 AI 服务改造 (`AdviceServiceImpl.java`)
修改 `AdviceServiceImpl.buildCreatePrompt` 方法：
1. 检查传入的 `Decision` 是否包含 `goalId`。
2. 如果包含，调用 `GoalService` 查出该目标的 `title` 和 `description`。
3. 在发送给 AI 的 Prompt 中追加如下上下文：
   ```text
   【用户的长期目标】
   标题：{goal.title}
   描述：{goal.description}
   
   请在分析以下选项时，额外评估每个选项是否有助于实现该长期目标，并在建议中给出明确的“目标契合度分析”。
   ```
4. （可选）修改 `DecisionAdviceResponse` 结构，让 AI 以结构化 JSON 的形式返回目标契合度的得分或专项评价。

### 3.4 前端界面设计 (Vue)
*   **侧边栏新增菜单**：增加“长期目标 (Goals)”菜单项。
*   **目标列表页 (`GoalListView.vue`)**：卡片式展示目标，显示标题、状态、倒计时等。
*   **目标详情页 (`GoalDetailView.vue`)**：上半部分展示目标详情，下半部分展示关联的决策历史表格。
*   **决策创建/编辑弹窗改造**：新增一个下拉框（Select 组件），调用 `GET /api/goals` 接口获取状态为 `IN_PROGRESS` 的目标供用户选择绑定。
*   **AI 建议展示页改造**：在展示 AI 分析结果的地方，增加一个区域专门显示“长期目标契合度分析”的内容。