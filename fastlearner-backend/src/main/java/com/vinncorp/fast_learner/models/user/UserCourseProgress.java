package com.vinncorp.fast_learner.models.user;

import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Getter
@Setter
@Entity
@Table(name = "user_course_progress",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"course_id", "topic_id", "student_id"})})
public class UserCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private Long seekTime;

    @Column(name = "is_completed")
    private boolean isCompleted = false;

    @Column(name = "created_date", updatable = false)
    @Temporal(TIMESTAMP)
    protected Date creationDate;


    @Column(name = "last_mod_date")
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;

}
