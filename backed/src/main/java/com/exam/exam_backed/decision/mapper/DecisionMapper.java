package com.exam.exam_backed.decision.mapper;

import com.exam.exam_backed.decision.Decision;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DecisionMapper {
    @Insert("""
            INSERT INTO decision (
                user_id, title, context, `options`, reason, tags, mood, urgency,
                review_time, satisfaction, feedback, status
            )
            VALUES (
                #{userId}, #{title}, #{context}, #{options}, #{reason}, #{tags}, #{mood}, #{urgency},
                #{reviewTime}, #{satisfaction}, #{feedback}, #{status}
            )
            """)
    int insert(Decision decision);

    @Select("SELECT LAST_INSERT_ID()")
    Long lastInsertedId();

    @Select("""
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            LIMIT 1
            """)
    Optional<Decision> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE user_id = #{userId} AND deleted = 0
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<Decision> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE user_id = #{userId} AND deleted = 0
            <if test="keyword != null and keyword != ''">
              AND title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test="tag != null and tag != ''">
              AND tags LIKE CONCAT('%', #{tag}, '%')
            </if>
            <if test="status != null and status != ''">
              AND status = #{status}
            </if>
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            </script>
            """)
    List<Decision> searchByUserId(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("tag") String tag,
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Select("""
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND status = 'pending'
              AND review_time <= NOW()
            ORDER BY review_time ASC, id DESC
            LIMIT #{limit}
            """)
    List<Decision> findDuePendingReviewByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM decision WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM decision WHERE user_id = #{userId} AND status = #{status} AND deleted = 0")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("""
            SELECT COUNT(*)
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND status = 'reviewed'
              AND satisfaction = #{satisfaction}
            """)
    int countReviewedBySatisfaction(@Param("userId") Long userId, @Param("satisfaction") String satisfaction);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND status = 'reviewed'
              AND satisfaction = #{satisfaction}
            <if test="tag != null and tag != ''">
              AND tags LIKE CONCAT('%', #{tag}, '%')
            </if>
            <if test="mood != null and mood != ''">
              AND mood = #{mood}
            </if>
            </script>
            """)
    int countReviewedBySatisfactionAndFilters(
            @Param("userId") Long userId,
            @Param("satisfaction") String satisfaction,
            @Param("tag") String tag,
            @Param("mood") String mood
    );

    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m') AS label, COUNT(*) AS count
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND create_time >= #{start}
              AND create_time < #{end}
            GROUP BY DATE_FORMAT(create_time, '%Y-%m')
            ORDER BY label ASC
            """)
    List<TrendCount> countCreatedByMonth(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Select("""
            SELECT DATE_FORMAT(create_time, '%d') AS label, COUNT(*) AS count
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND create_time >= #{start}
              AND create_time < #{end}
            GROUP BY DATE_FORMAT(create_time, '%d')
            ORDER BY label ASC
            """)
    List<TrendCount> countCreatedByDay(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Select("""
            SELECT tags
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND tags IS NOT NULL
              AND tags <> ''
            """)
    List<String> findTagsByUserId(@Param("userId") Long userId);

    record TrendCount(String label, int count) {
    }

    @Update("""
            UPDATE decision
            SET satisfaction = #{satisfaction},
                feedback = #{feedback},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int updateReview(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("satisfaction") String satisfaction,
            @Param("feedback") String feedback,
            @Param("status") String status
    );

    @Update("""
            UPDATE decision
            SET deleted = 1,
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}
