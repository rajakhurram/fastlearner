package com.vinncorp.fast_learner.es_dto;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicContentResponse {
    private String id;
    private Long topicId;
    private Long sectionId;
    private String name;
    private Double score;
}
