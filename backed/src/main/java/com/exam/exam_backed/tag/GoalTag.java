package com.exam.exam_backed.tag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("goal_tag")
public class GoalTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goalId;
    private Long tagId;

    public GoalTag() {
    }

    public GoalTag(Long id, Long goalId, Long tagId) {
        this.id = id;
        this.goalId = goalId;
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
