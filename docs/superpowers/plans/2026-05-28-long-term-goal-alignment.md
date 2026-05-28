# 长期目标关联 (Long-term Goal Alignment) 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**目标：** 实现“长期目标关联”及“标签系统”，支持智能推荐目标。
**UI 要求：** 必须读取 `D:\desktop\store\Exam_final\long-term-main-page-main` 中的 React 源码，复刻其 UI 设计，但必须使用 Vue 3 (Composition API) 编写。

---

### Task 1: 数据库表创建

**文件:**
- Create: `backed/src/main/resources/db/migration/V3__add_goal_and_tags.sql` (如果项目不用 Flyway/Liquibase，请指导用户在本地 MySQL 直接执行)

- [ ] **Step 1: 编写并执行 SQL**

```sql
CREATE TABLE goal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    target_date DATE,
    measurement VARCHAR(255),
    progress INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_tag (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE goal_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goal_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_goal_tag (goal_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE decision_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    decision_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    UNIQUE KEY uk_decision_tag (decision_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE decision ADD COLUMN goal_id BIGINT DEFAULT NULL;
```

### Task 2: 后端 - 实体类与 Mapper 定义

**文件:**
- Create: `backed/src/main/java/com/exam/exam_backed/goal/Goal.java`
- Create: `backed/src/main/java/com/exam/exam_backed/tag/Tag.java`
- Create: `backed/src/main/java/com/exam/exam_backed/tag/GoalTag.java`
- Create: `backed/src/main/java/com/exam/exam_backed/tag/DecisionTag.java`
- Create: 对应的 Mapper 接口

- [ ] **Step 1: 编写 Goal 实体**

```java
package com.exam.exam_backed.goal;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("goal")
public class Goal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private LocalDate targetDate;
    private String measurement;
    private Integer progress;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 编写 Tag 相关实体**
(为 `Tag`, `GoalTag`, `DecisionTag` 编写类似的 `@Data` 和 `@TableName` 实体类，映射数据库字段)

- [ ] **Step 3: 编写 Mapper**
创建 `GoalMapper`, `TagMapper`, `GoalTagMapper`, `DecisionTagMapper` 接口，全部继承 `BaseMapper<T>` 并打上 `@Mapper` 注解。

### Task 3: 后端 - Tag 服务实现 (核心推荐逻辑)

**文件:**
- Create: `backed/src/main/java/com/exam/exam_backed/tag/service/TagService.java`
- Create: `backed/src/main/java/com/exam/exam_backed/tag/service/impl/TagServiceImpl.java`

- [ ] **Step 1: 实现标签保存与解析逻辑**

```java
// 在 TagServiceImpl 中实现：
public List<Tag> getOrCreateTags(Long userId, List<String> tagNames) {
    if (tagNames == null || tagNames.isEmpty()) return Collections.emptyList();
    List<Tag> result = new ArrayList<>();
    for (String name : tagNames) {
        String trimmed = name.trim();
        Tag exist = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId).eq(Tag::getName, trimmed));
        if (exist == null) {
            Tag newTag = new Tag();
            newTag.setUserId(userId);
            newTag.setName(trimmed);
            tagMapper.insert(newTag);
            result.add(newTag);
        } else {
            result.add(exist);
        }
    }
    return result;
}
```

- [ ] **Step 2: 实现标签与目标的绑定**

```java
public void bindGoalTags(Long goalId, List<Long> tagIds) {
    goalTagMapper.delete(new LambdaQueryWrapper<GoalTag>().eq(GoalTag::getGoalId, goalId));
    for (Long tagId : tagIds) {
        GoalTag gt = new GoalTag();
        gt.setGoalId(goalId);
        gt.setTagId(tagId);
        goalTagMapper.insert(gt);
    }
}
// 同理实现 bindDecisionTags
```

### Task 4: 后端 - Goal 服务与智能推荐 API

**文件:**
- Create: `GoalService.java`, `GoalServiceImpl.java`, `GoalController.java`

- [ ] **Step 1: 实现目标 CRUD (带标签处理)**
在 `createGoal` 中：
```java
Goal goal = new Goal();
// ... 赋值 ...
goalMapper.insert(goal);
if (request.getTags() != null && !request.getTags().isEmpty()) {
    List<Tag> tags = tagService.getOrCreateTags(userId, request.getTags());
    tagService.bindGoalTags(goal.getId(), tags.stream().map(Tag::getId).collect(Collectors.toList()));
}
```

- [ ] **Step 2: 实现推荐算法 `recommendGoalsByTags`**
```java
// GoalServiceImpl
public List<Goal> recommendGoalsByTags(Long userId, List<String> tagNames) {
    List<Tag> tags = tagService.getOrCreateTags(userId, tagNames);
    if (tags.isEmpty()) return Collections.emptyList();
    List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
    
    // 找出包含这些tag_id的goal_id，按匹配数量倒序
    List<GoalTag> goalTags = goalTagMapper.selectList(new LambdaQueryWrapper<GoalTag>().in(GoalTag::getTagId, tagIds));
    Map<Long, Long> goalMatchCount = goalTags.stream().collect(Collectors.groupingBy(GoalTag::getGoalId, Collectors.counting()));
    
    List<Long> topGoalIds = goalMatchCount.entrySet().stream()
        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
        .limit(3).map(Map.Entry::getKey).collect(Collectors.toList());
        
    if (topGoalIds.isEmpty()) return Collections.emptyList();
    return goalMapper.selectBatchIds(topGoalIds);
}
```

- [ ] **Step 3: 暴露 Controller 接口**
创建 `POST /api/goals`, `GET /api/goals`, `POST /api/goals/recommend` 等接口。

### Task 5: 后端 - 决策流程整合与 AI 增强

**文件:**
- Modify: `DecisionServiceImpl.java`, `AdviceServiceImpl.java`

- [ ] **Step 1: 修改 Decision 创建逻辑**
在 `DecisionServiceImpl.createDecision` 中，获取 `request.getGoalId()` 并赋给 decision。同时解析 `request.getTags()` 调用 `tagService.bindDecisionTags`。

- [ ] **Step 2: 增强 AI Prompt**
在 `AdviceServiceImpl.buildCreatePrompt` 中：
```java
if (decision.getGoalId() != null) {
    Goal goal = goalMapper.selectById(decision.getGoalId());
    if (goal != null) {
        sb.append("\n【长期目标】：").append(goal.getTitle()).append(" - ").append(goal.getDescription());
        sb.append("\n要求：分析每个选项对达成该长期目标是否有帮助，并在返回的JSON中加入 goalAlignment 字段，包含 score(契合度0-100), bestOption 和 reason。");
    }
}
```

### Task 6: 前端 - API 封装与路由

**文件:**
- Create: `fronted/src/api/goal.js`
- Modify: `fronted/src/router/index.js`

- [ ] **Step 1: 编写 Axios 请求**
```javascript
import request from '../utils/request'
export const recommendGoals = (tags) => request.post('/api/goals/recommend', { tags })
export const getGoals = () => request.get('/api/goals')
// ... 其他 CRUD
```

- [ ] **Step 2: 注册路由**
注册 `/goals` (指向 `GoalListView`) 和 `/goals/:id` (指向 `GoalDetailView`)。

### Task 7: 前端 - 转化 React 为 Vue 3 (目标列表页)

**文件:**
- Create: `fronted/src/views/goal/GoalListView.vue`

- [ ] **Step 1: 读取外部 React 代码**
AI 必须读取 `D:\desktop\store\Exam_final\long-term-main-page-main\src\pages\GoalList.jsx` (或对应的页面文件) 和其 CSS。

- [ ] **Step 2: 用 Vue 3 (Script Setup) 重新实现**
```vue
<template>
  <!-- 将 React 的 JSX 转换为 Vue Template -->
  <!-- 必须保留原始 CSS 类名和布局结构 -->
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getGoals } from '@/api/goal'
// 将 React 的 useState 和 useEffect 转换为 ref 和 onMounted
</script>
<style scoped>
/* 复制 React 项目中的对应 CSS */
</style>
```

### Task 8: 前端 - 转化 React 为 Vue 3 (目标详情页)

**文件:**
- Create: `fronted/src/views/goal/GoalDetailView.vue`

- [ ] **Step 1: 翻译详情页 UI**
读取 React 版本的 Detail 页面。复刻进度条、状态展示区。

- [ ] **Step 2: 绑定决策列表**
底部添加一个区域，调用 API 获取并展示该目标下的所有 Decision 卡片。

### Task 9: 前端 - 决策表单智能推荐

**文件:**
- Modify: `fronted/src/views/dashboard/DashboardView.vue` (或对应的决策表单)

- [ ] **Step 1: 标签输入与推荐联动**
```javascript
import { recommendGoals } from '@/api/goal'
const tags = ref([]) // 绑定的标签
const recommendedGoals = ref([])

// 监听 tags 变化或失焦时调用：
const fetchRecommendations = async () => {
  if (tags.value.length === 0) return;
  const res = await recommendGoals(tags.value);
  recommendedGoals.value = res.data.data;
}
```

- [ ] **Step 2: 渲染推荐 UI 与 AI 结果**
在表单下方渲染推荐块。在展示 AI 结果时，读取 `advice.goalAlignment.score` 和 `reason` 进行展示。
