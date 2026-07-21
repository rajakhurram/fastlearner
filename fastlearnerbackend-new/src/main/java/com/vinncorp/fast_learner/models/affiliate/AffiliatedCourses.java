package com.vinncorp.fast_learner.models.affiliate;


import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "affiliated_courses")
public class AffiliatedCourses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instructor_affiliate")
    private InstructorAffiliate instructorAffiliate;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private Long onboardedStudents;

    @Temporal(TemporalType.TIMESTAMP)
    private Date assignDate;

    @Column(name = "reward",nullable = false)
    private Double reward;
    private Double revenue;
    private String url;
    @Enumerated(value = EnumType.STRING)
    private GenericStatus status ;

}
