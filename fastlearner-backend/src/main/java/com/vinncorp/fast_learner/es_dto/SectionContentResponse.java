package com.vinncorp.fast_learner.es_dto;

import com.vinncorp.fast_learner.es_models.TopicContent;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionContentResponse {
    private String id;
    private Long sectionId;
    private String name;
    private Double score;
}
