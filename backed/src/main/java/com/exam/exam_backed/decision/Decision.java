package com.exam.exam_backed.decision;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("decision")
public class Decision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goalId;
    private String title;
    private String context;
    @TableField("`options`")
    private String options;
    private String reason;
    private String tags;
    private String mood;
    private Integer urgency;
    private LocalDateTime reviewTime;
    private String satisfaction;
    private String feedback;
    private String status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Decision() {
    }

    public Decision(
            Long id,
            Long userId,
            String title,
            String context,
            String options,
            String reason,
            String tags,
            String mood,
            Integer urgency,
            LocalDateTime reviewTime,
            String satisfaction,
            String feedback,
            String status,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        this(id, userId, null, title, context, options, reason, tags, mood, urgency, reviewTime,
                satisfaction, feedback, status, 0, createTime, updateTime);
    }

    public Decision(
            Long id,
            Long userId,
            Long goalId,
            String title,
            String context,
            String options,
            String reason,
            String tags,
            String mood,
            Integer urgency,
            LocalDateTime reviewTime,
            String satisfaction,
            String feedback,
            String status,
            Integer deleted,
            LocalDateTime createTime,
            LocalDateTime updateTime
    ) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.title = title;
        this.context = context;
        this.options = options;
        this.reason = reason;
        this.tags = tags;
        this.mood = mood;
        this.urgency = urgency;
        this.reviewTime = reviewTime;
        this.satisfaction = satisfaction;
        this.feedback = feedback;
        this.status = status;
        this.deleted = deleted == null ? 0 : deleted;
        this.createTime = createTime;
        this.updateTime = updateTime;
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

    public Long userId() {
        return userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String title() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String context() {
        return context;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String options() {
        return options;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String reason() {
        return reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String tags() {
        return tags;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String mood() {
        return mood;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Integer urgency() {
        return urgency;
    }

    public Integer getUrgency() {
        return urgency;
    }

    public void setUrgency(Integer urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime reviewTime() {
        return reviewTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String satisfaction() {
        return satisfaction;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(String satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String feedback() {
        return feedback;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String status() {
        return status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer deleted() {
        return deleted;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime createTime() {
        return createTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime updateTime() {
        return updateTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Decision withId(Long newId) {
        return new Decision(newId, userId, goalId, title, context, options, reason, tags, mood, urgency, reviewTime,
                satisfaction, feedback, status, deleted, createTime, updateTime);
    }

    public Decision withReview(String newSatisfaction, String newFeedback, String newStatus) {
        return new Decision(id, userId, goalId, title, context, options, reason, tags, mood, urgency, reviewTime,
                newSatisfaction, newFeedback, newStatus, deleted, createTime, LocalDateTime.now());
    }
}
