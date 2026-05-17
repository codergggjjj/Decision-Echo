package com.exam.exam_backed.decision.mapper;

import com.exam.exam_backed.decision.Decision;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            WHERE id = #{id} AND user_id = #{userId}
            LIMIT 1
            """)
    Optional<Decision> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE user_id = #{userId}
            ORDER BY create_time DESC, id DESC
            LIMIT #{limit}
            """)
    List<Decision> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT id, user_id AS userId, title, context, `options`, reason, tags, mood, urgency,
                   review_time AS reviewTime, satisfaction, feedback, status,
                   create_time AS createTime, update_time AS updateTime
            FROM decision
            WHERE user_id = #{userId}
              AND status = 'pending'
              AND review_time <= NOW()
            ORDER BY review_time ASC, id DESC
            LIMIT #{limit}
            """)
    List<Decision> findDuePendingReviewByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM decision WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM decision WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("""
            SELECT COUNT(*)
            FROM decision
            WHERE user_id = #{userId}
              AND status = 'reviewed'
              AND satisfaction = #{satisfaction}
            """)
    int countReviewedBySatisfaction(@Param("userId") Long userId, @Param("satisfaction") String satisfaction);

    @Update("""
            UPDATE decision
            SET satisfaction = #{satisfaction},
                feedback = #{feedback},
                status = #{status},
                update_time = NOW()
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int updateReview(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("satisfaction") String satisfaction,
            @Param("feedback") String feedback,
            @Param("status") String status
    );
}
