package com.exam.exam_backed.tag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("decision_tag")
public class DecisionTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long decisionId;
    private Long tagId;

    public DecisionTag() {
    }

    public DecisionTag(Long id, Long decisionId, Long tagId) {
        this.id = id;
        this.decisionId = decisionId;
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Long decisionId) {
        this.decisionId = decisionId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
