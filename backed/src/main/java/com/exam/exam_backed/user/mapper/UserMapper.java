package com.exam.exam_backed.user.mapper;

import com.exam.exam_backed.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT
                id,
                username,
                password_hash AS passwordHash,
                nickname,
                status
            FROM user
            WHERE username = #{username}
            LIMIT 1
            """)
    Optional<User> findByUsername(String username);
}
