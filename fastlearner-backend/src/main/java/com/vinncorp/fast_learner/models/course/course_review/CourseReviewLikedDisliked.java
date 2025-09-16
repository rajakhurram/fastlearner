package com.vinncorp.fast_learner.models.course.course_review;

import com.vinncorp.fast_learner.util.enums.CourseReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_review_liked_disliked")
public class CourseReviewLikedDisliked {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ids", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CourseReviewStatus status;

    @Column(name = "course_review_id")
    private Long courseReviewId;

    @Column(name = "created_at", updatable = false)
    @Temporal(TIMESTAMP)
    private Date createdAt;

    @Column(name="created_by")
    protected Long createdBy;
}
