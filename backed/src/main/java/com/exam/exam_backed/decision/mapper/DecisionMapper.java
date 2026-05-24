package com.exam.exam_backed.decision.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.exam_backed.decision.Decision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DecisionMapper extends BaseMapper<Decision> {
    default Optional<Decision> findByIdAndUserId(Long id, Long userId) {
        return Optional.ofNullable(selectOne(baseUserQuery(userId)
                .eq("id", id)
                .last("LIMIT 1")));
    }

    default List<Decision> findRecentByUserId(Long userId, int limit) {
        return selectList(baseUserQuery(userId)
                .orderByDesc("create_time", "id")
                .last("LIMIT " + limit));
    }

    default List<Decision> searchByUserId(Long userId, String keyword, String tag, String status, int limit) {
        QueryWrapper<Decision> query = baseUserQuery(userId);
        if (keyword != null && !keyword.isBlank()) {
            query.like("title", keyword);
        }
        if (tag != null && !tag.isBlank()) {
            query.like("tags", tag);
        }
        if (status != null && !status.isBlank()) {
            query.eq("status", status);
        }
        return selectList(query.orderByDesc("create_time", "id").last("LIMIT " + limit));
    }

    default List<Decision> findDuePendingReviewByUserId(Long userId, int limit) {
        return selectList(baseUserQuery(userId)
                .eq("status", "pending")
                .le("review_time", LocalDateTime.now())
                .orderByAsc("review_time")
                .orderByDesc("id")
                .last("LIMIT " + limit));
    }

    default int countByUserId(Long userId) {
        return selectCount(baseUserQuery(userId)).intValue();
    }

    default int countByUserIdAndStatus(Long userId, String status) {
        return selectCount(baseUserQuery(userId).eq("status", status)).intValue();
    }

    default int countReviewedBySatisfaction(Long userId, String satisfaction) {
        return selectCount(baseUserQuery(userId)
                .eq("status", "reviewed")
                .eq("satisfaction", satisfaction)).intValue();
    }

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
            SELECT mood, satisfaction, COUNT(*) AS count
            FROM decision
            WHERE user_id = #{userId}
              AND deleted = 0
              AND status = 'reviewed'
              AND mood IS NOT NULL
              AND mood <> ''
              AND satisfaction IS NOT NULL
              AND satisfaction <> ''
            GROUP BY mood, satisfaction
            """)
    List<MoodSatisfactionCount> countReviewedByMoodAndSatisfaction(@Param("userId") Long userId);

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

    record MoodSatisfactionCount(String mood, String satisfaction, int count) {
    }

    default int updateReview(Long id, Long userId, String satisfaction, String feedback, String status) {
        return update(null, new UpdateWrapper<Decision>()
                .eq("id", id)
                .eq("user_id", userId)
                .eq("deleted", 0)
                .set("satisfaction", satisfaction)
                .set("feedback", feedback)
                .set("status", status)
                .setSql("update_time = NOW()"));
    }

    default int softDelete(Long id, Long userId) {
        return update(null, new UpdateWrapper<Decision>()
                .eq("id", id)
                .eq("user_id", userId)
                .eq("deleted", 0)
                .set("deleted", 1)
                .setSql("update_time = NOW()"));
    }

    private QueryWrapper<Decision> baseUserQuery(Long userId) {
        return new QueryWrapper<Decision>()
                .eq("user_id", userId)
                .eq("deleted", 0);
    }
}
