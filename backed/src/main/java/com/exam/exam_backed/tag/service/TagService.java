package com.exam.exam_backed.tag.service;

import com.exam.exam_backed.tag.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getOrCreateTags(Long userId, List<String> tagNames);

    List<Tag> getOrCreateTags(Long userId, String rawTags);

    void bindGoalTags(Long goalId, List<Long> tagIds);

    void bindDecisionTags(Long decisionId, List<Long> tagIds);

    List<String> splitTagNames(String rawTags);
}
