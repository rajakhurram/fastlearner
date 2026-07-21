package com.vinncorp.fast_learner.es_dto;

import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoredItem {
    private String id;
    private String name;
    private Double score;
    private Long sectionId;
    private Long topicId;
    private String type;
}
