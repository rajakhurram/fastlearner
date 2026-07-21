package com.vinncorp.fast_learner.controllers.youtube_video.user;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveStudentsResponse {
    private int numericMonth;
    private String monthName;
    private long totalStudents;

    public static List<ActiveStudentsResponse> from(List<Tuple> data) {
        return data.stream().map(e -> ActiveStudentsResponse.builder()
                .monthName((String) e.get("month_name"))
                .numericMonth(Integer.parseInt("" + e.get("month")))
                .totalStudents(Long.parseLong("" + e.get("active_students")))
                .build()).toList();
    }
}
