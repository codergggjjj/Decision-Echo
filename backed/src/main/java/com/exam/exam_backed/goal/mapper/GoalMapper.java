package com.exam.exam_backed.goal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.exam_backed.goal.Goal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoalMapper extends BaseMapper<Goal> {
}
