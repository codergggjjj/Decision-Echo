package com.exam.exam_backed.advice.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.exam_backed.advice.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    default Optional<String> findValueByKey(String configKey) {
        return Optional.ofNullable(selectOne(new QueryWrapper<SystemConfig>()
                        .eq("config_key", configKey)
                        .eq("deleted", 0)
                        .last("LIMIT 1")))
                .map(SystemConfig::getConfigValue);
    }
}
