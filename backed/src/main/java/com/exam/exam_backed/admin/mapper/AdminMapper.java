package com.exam.exam_backed.admin.mapper;

import com.exam.exam_backed.admin.vo.AdminDecisionVO;
import com.exam.exam_backed.admin.vo.AdminUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminMapper {
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();

    @Select("SELECT COUNT(*) FROM decision WHERE deleted = 0")
    int countDecisions();

    @Select("SELECT COUNT(*) FROM decision WHERE status = #{status} AND deleted = 0")
    int countDecisionsByStatus(String status);

    @Select("""
            <script>
            SELECT id, username, nickname, avatar_url AS avatarUrl, status, role, create_time AS createTime
            FROM user
            <where>
              <if test="keyword != null and keyword != ''">
                username LIKE CONCAT('%', #{keyword}, '%')
                OR nickname LIKE CONCAT('%', #{keyword}, '%')
              </if>
            </where>
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            </script>
            """)
    List<AdminUserVO> findUsers(@Param("keyword") String keyword, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT d.id,
                   d.user_id AS userId,
                   u.username AS username,
                   d.title,
                   d.tags,
                   d.mood,
                   d.urgency,
                   d.review_time AS reviewTime,
                   d.satisfaction,
                   d.status,
                   d.create_time AS createTime
            FROM decision d
            INNER JOIN user u ON u.id = d.user_id
            WHERE d.deleted = 0
            <if test="keyword != null and keyword != ''">
              AND (d.title LIKE CONCAT('%', #{keyword}, '%')
                   OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="status != null and status != ''">
              AND d.status = #{status}
            </if>
            ORDER BY d.create_time DESC, d.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<AdminDecisionVO> findDecisions(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("limit") int limit
    );
}
