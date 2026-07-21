package com.vinncorp.fast_learner.models.course;

import com.vinncorp.fast_learner.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "course_visitor")
public class CourseVisitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private User visitor;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "instructor_id")
    private Long instructorId;

    private LocalDateTime visitedAt;

}
