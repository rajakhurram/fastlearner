package com.vinncorp.fast_learner.models.favourite_course;

import com.vinncorp.fast_learner.models.audit.Auditable;
import com.vinncorp.fast_learner.models.course.Course;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favourite_course")
public class FavouriteCourse extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "is_active")
    private boolean isActive = false;
}
