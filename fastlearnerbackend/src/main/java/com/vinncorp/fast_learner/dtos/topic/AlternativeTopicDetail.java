package com.vinncorp.fast_learner.dtos.topic;

import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AlternativeTopicDetail {

    private Long courseId;
    private Long instructorId;
    private String instructorName;
    private String instructorImage;
    private int totalReviewer;
    private double totalReviews;
    private Long topicId;
    private String topicName;
    private Long sectionId;

    public static List<AlternativeTopicDetail> from(List<Tuple> data) {
        return data.stream()
                .map(e -> AlternativeTopicDetail.builder()
                        .courseId((Long) e.get("course_id"))
                        .topicId((Long) e.get("topic_id"))
                        .topicName((String) e.get("topic_name"))
                        .instructorId((Long) e.get("instructor_id"))
                        .instructorName((String) e.get("instructor_name"))
                        .instructorImage((String) e.get("profile_picture"))
                        .sectionId((Long) e.get("section_id"))
                        .totalReviewer(e.get("total_reviews") != null ? Integer.parseInt("" + e.get("total_reviews")) : 0 )
                        .totalReviews(e.get("avg_section_rating") != null ? Double.parseDouble("" + e.get("avg_section_rating")) : 0)
                        .build())
                .toList();
    }
}
