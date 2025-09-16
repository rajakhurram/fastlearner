package com.vinncorp.fast_learner.dtos.user.user_course_progress;

import jakarta.persistence.Tuple;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursesProgress {

    private Long id;
    private Double percentage;

    public static List<CoursesProgress> from(List<Tuple> m) {
        return m.stream().map(e -> CoursesProgress.builder()
                .id((Long) e.get("id"))
                .percentage(Double.parseDouble("" + e.get("completion_percentage")))
                .build()).toList();
    }
}
