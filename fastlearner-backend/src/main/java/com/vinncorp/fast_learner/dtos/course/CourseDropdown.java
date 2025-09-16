package com.vinncorp.fast_learner.dtos.course;

import jakarta.persistence.Tuple;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDropdown {

    private Long id;
    private String title;

    public static List<CourseDropdown> from(List<Tuple> data) {
        return data.stream().map(e -> CourseDropdown.builder()
                .id(Objects.nonNull(e.get("id")) ? Long.parseLong("" + e.get("id")): null)
                .title("" + e.get("title"))
                .build()).toList();
    }
}
