package com.vinncorp.fast_learner.dtos.topic;

import jakarta.persistence.Tuple;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoOfTopicInCourse {
    private Long courseId;
    private Integer topics;
    private Integer duration;

    public static List<NoOfTopicInCourse> from(List<Tuple> data) {
        List<NoOfTopicInCourse> mappedData = new ArrayList<>();
        return data.stream().map(e -> {
            NoOfTopicInCourse d = new NoOfTopicInCourse();
            d.setCourseId((Long) e.get("course_id"));
            d.setTopics(e.get("total_topics") != null ? Integer.parseInt("" + e.get("total_topics")) : 0);
            d.setDuration(e.get("duration") != null ? Integer.parseInt("" + e.get("duration")) : 0);
            return d;
        }).collect(Collectors.toList());
    }
}
