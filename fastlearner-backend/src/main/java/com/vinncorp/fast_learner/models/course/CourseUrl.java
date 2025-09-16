package com.vinncorp.fast_learner.models.course;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "course_url")
public class CourseUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String url;
    @Enumerated(value = EnumType.STRING)
    private GenericStatus status;
    @ManyToOne
    Course course;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseUrl courseUrl = (CourseUrl) o;
        return Objects.equals(url, courseUrl.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
