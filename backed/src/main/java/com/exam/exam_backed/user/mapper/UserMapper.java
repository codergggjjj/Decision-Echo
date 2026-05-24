package com.exam.exam_backed.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.exam_backed.user.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectOne(new QueryWrapper<User>()
                .eq("username", username)
                .last("LIMIT 1")));
    }

    default Optional<User> findById(Long id) {
        return Optional.ofNullable(selectById(id));
    }

    default int updatePassword(Long id, String passwordHash) {
        return update(new UpdateWrapper<User>()
                .eq("id", id)
                .set("password_hash", passwordHash)
                .setSql("update_time = NOW()"));
    }

    default int updateProfile(Long id, String nickname, String avatarUrl) {
        return update(new UpdateWrapper<User>()
                .eq("id", id)
                .set("nickname", nickname)
                .set("avatar_url", avatarUrl)
                .setSql("update_time = NOW()"));
    }
}
