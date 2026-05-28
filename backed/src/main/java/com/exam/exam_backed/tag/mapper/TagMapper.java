package com.exam.exam_backed.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.exam_backed.tag.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
