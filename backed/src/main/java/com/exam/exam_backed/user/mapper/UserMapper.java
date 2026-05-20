package com.exam.exam_backed.user.mapper;

import com.exam.exam_backed.user.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT
                id,
                username,
                password_hash AS passwordHash,
                nickname,
                avatar_url AS avatarUrl,
                status,
                create_time AS createTime
            FROM user
            WHERE username = #{username}
            LIMIT 1
            """)
    Optional<User> findByUsername(String username);

    @Select("""
            SELECT
                id,
                username,
                password_hash AS passwordHash,
                nickname,
                avatar_url AS avatarUrl,
                status,
                create_time AS createTime
            FROM user
            WHERE id = #{id}
            LIMIT 1
            """)
    Optional<User> findById(Long id);

    @Insert("""
            INSERT INTO user (username, password_hash, nickname, avatar_url, status)
            VALUES (#{username}, #{passwordHash}, #{nickname}, #{avatarUrl}, #{status})
            """)
    int insert(User user);

    @Update("""
            UPDATE user
            SET password_hash = #{passwordHash},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Update("""
            UPDATE user
            SET nickname = #{nickname},
                avatar_url = #{avatarUrl},
                update_time = NOW()
            WHERE id = #{id}
            """)
    int updateProfile(@Param("id") Long id, @Param("nickname") String nickname, @Param("avatarUrl") String avatarUrl);
}
