package com.exam.exam_backed.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.exam_backed.tag.DecisionTag;
import com.exam.exam_backed.tag.GoalTag;
import com.exam.exam_backed.tag.Tag;
import com.exam.exam_backed.tag.mapper.DecisionTagMapper;
import com.exam.exam_backed.tag.mapper.GoalTagMapper;
import com.exam.exam_backed.tag.mapper.TagMapper;
import com.exam.exam_backed.tag.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;
    private final GoalTagMapper goalTagMapper;
    private final DecisionTagMapper decisionTagMapper;

    public TagServiceImpl(TagMapper tagMapper, GoalTagMapper goalTagMapper, DecisionTagMapper decisionTagMapper) {
        this.tagMapper = tagMapper;
        this.goalTagMapper = goalTagMapper;
        this.decisionTagMapper = decisionTagMapper;
    }

    @Override
    @Transactional
    public List<Tag> getOrCreateTags(Long userId, List<String> tagNames) {
        List<String> normalizedNames = normalizeTagNames(tagNames);
        if (normalizedNames.isEmpty()) {
            return List.of();
        }
        List<Tag> result = new ArrayList<>();
        for (String name : normalizedNames) {
            Tag existing = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getUserId, userId)
                    .eq(Tag::getName, name)
                    .last("LIMIT 1"));
            if (existing != null) {
                result.add(existing);
                continue;
            }
            Tag tag = new Tag();
            tag.setUserId(userId);
            tag.setName(name);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            tagMapper.insert(tag);
            result.add(tag);
        }
        return result;
    }

    @Override
    public List<Tag> getOrCreateTags(Long userId, String rawTags) {
        return getOrCreateTags(userId, splitTagNames(rawTags));
    }

    @Override
    @Transactional
    public void bindGoalTags(Long goalId, List<Long> tagIds) {
        goalTagMapper.delete(new LambdaQueryWrapper<GoalTag>().eq(GoalTag::getGoalId, goalId));
        for (Long tagId : normalizeIds(tagIds)) {
            GoalTag goalTag = new GoalTag();
            goalTag.setGoalId(goalId);
            goalTag.setTagId(tagId);
            goalTagMapper.insert(goalTag);
        }
    }

    @Override
    @Transactional
    public void bindDecisionTags(Long decisionId, List<Long> tagIds) {
        decisionTagMapper.delete(new LambdaQueryWrapper<DecisionTag>().eq(DecisionTag::getDecisionId, decisionId));
        for (Long tagId : normalizeIds(tagIds)) {
            DecisionTag decisionTag = new DecisionTag();
            decisionTag.setDecisionId(decisionId);
            decisionTag.setTagId(tagId);
            decisionTagMapper.insert(decisionTag);
        }
    }

    @Override
    public List<String> splitTagNames(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }
        String[] values = rawTags.split("[,，、\\s]+");
        List<String> tags = new ArrayList<>();
        for (String value : values) {
            String tag = value.trim();
            if (!tag.isBlank()) {
                tags.add(tag);
            }
        }
        return normalizeTagNames(tags);
    }

    private List<String> normalizeTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : tagNames) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isBlank()) {
                result.add(trimmed);
            }
        }
        return new ArrayList<>(result);
    }

    private List<Long> normalizeIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        for (Long tagId : tagIds) {
            if (tagId != null) {
                result.add(tagId);
            }
        }
        return new ArrayList<>(result);
    }
}
