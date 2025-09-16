package com.vinncorp.fast_learner.models.user;

import com.vinncorp.fast_learner.models.course.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_course_completion")
public class UserCourseCompletion {

    @Transient
    public static final int MINIMUM_TOPIC_COMPLETION_PERCENTAGE = 99;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date creationDate;
}
