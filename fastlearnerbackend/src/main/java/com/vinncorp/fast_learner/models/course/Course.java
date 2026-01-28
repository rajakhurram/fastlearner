package com.vinncorp.fast_learner.models.course;

import com.vinncorp.fast_learner.models.audit.Auditable;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "course")
public class Course extends Auditable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;


    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @ManyToOne
    @JoinColumn(name = "course_category_id")
    private CourseCategory courseCategory;

    private Integer courseDurationInHours;

    @ManyToOne
    @JoinColumn(name = "course_level")
    private CourseLevel courseLevel;

    @Column(name = "prerequisite", columnDefinition = "TEXT")
    private String prerequisite;

    @Column(name = "course_outcome", columnDefinition = "TEXT")
    private String courseOutcome;


    /**
     * If db first approach then run below queries in postgres
     *
     * ALTER TABLE course
     * ADD COLUMN document_vector tsvector;
     *
     * UPDATE course
     * SET document_vector = to_tsvector('english', coalesce(title, '') || ' ' || coalesce(about, '') || ' '
     * 								  || coalesce((SELECT STRING_AGG(t.name, ' ') AS tag_names FROM course_tag as ct INNER JOIN tag as t ON t.id = ct.tag_id where course_id = course.id GROUP BY ct.course_id), '') || ' '
     * 								  || coalesce((SELECT name FROM course_category WHERE id = course_category_id), ''));
     * Now create index of this column
     * CREATE INDEX idx_course_document_vector ON course USING gin(document_vector);
     *
     * FETCH BY
     * SELECT * FROM course WHERE document_vector @@ to_tsquery('english', 'java');
     * */
    // CREATE INDEX idx_course_title ON course USING gin(document_vector gin_trgm_ops);
    @Column(name = "document_vector", columnDefinition = "TEXT")
    private String documentVector;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;
    private String thumbnail;
    private String previewVideoURL;
    @Column(name = "preview_video_vtt_content", columnDefinition = "TEXT")
    private String previewVideoVttContent;
    private Boolean certificateEnabled = false;
    private String courseProgress;
    private String metaHeading;
    private String metaTitle;
    private String metaDescription;
    @Enumerated(value = EnumType.STRING)
    private CourseStatus courseStatus;
    @Enumerated(value = EnumType.STRING)
    private CourseType courseType;
    private Double price;
    @Enumerated(value = EnumType.STRING)
    private ContentType contentType;
}