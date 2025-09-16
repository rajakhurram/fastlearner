package com.vinncorp.fast_learner.response.course;

import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseVisitorResponse {

    private int numericMonth;
    private String monthName;
    private long totalVisitors;

    public static List<CourseVisitorResponse> from(List<Tuple> data) {
        return data.stream().map(e -> CourseVisitorResponse.builder()
                    .monthName((String) e.get("month_name"))
                    .numericMonth(Integer.parseInt("" + e.get("month")))
                    .totalVisitors(Long.parseLong("" + e.get("visit_count")))
                    .build()).toList();
    }
}
