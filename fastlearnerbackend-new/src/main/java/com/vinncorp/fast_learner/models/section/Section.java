package com.vinncorp.fast_learner.models.section;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.topic.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "section")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "is_free")
    private boolean isFree = true;
    private int sequenceNumber;
    private boolean isActive = true;

    @Transient
    private Boolean delete;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = CascadeType.REMOVE)
    private List<Topic> topics;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = CascadeType.REMOVE)
    private List<SectionReview> sectionReviews;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = CascadeType.REMOVE)
    private List<UserAlternateSection> sectionId;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fromSection", cascade = CascadeType.REMOVE)
    private List<UserAlternateSection> fromSection;

}