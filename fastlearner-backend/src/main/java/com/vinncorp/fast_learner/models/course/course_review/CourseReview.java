package com.vinncorp.fast_learner.models.course.course_review;

import com.vinncorp.fast_learner.models.audit.Auditable;
import com.vinncorp.fast_learner.models.course.Course;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_review")
public class CourseReview extends Auditable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    private double rating;

    @Column(name = "likes", columnDefinition = "INT default 0")
    private int likes;

    @Column(name = "dislikes", columnDefinition = "INT default 0")
    private int dislikes;
}