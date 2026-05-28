package com.exam.exam_backed.decision;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("decision_goal")
public class DecisionGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long decisionId;
    private Long goalId;

    public DecisionGoal() {
    }

    public DecisionGoal(Long id, Long decisionId, Long goalId) {
        this.id = id;
        this.decisionId = decisionId;
        this.goalId = goalId;
    }

    public Long id() {
        return id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long decisionId() {
        return decisionId;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Long decisionId) {
        this.decisionId = decisionId;
    }

    public Long goalId() {
        return goalId;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }
}
