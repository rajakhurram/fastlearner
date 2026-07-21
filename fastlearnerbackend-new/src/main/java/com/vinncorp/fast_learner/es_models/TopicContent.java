package com.vinncorp.fast_learner.es_models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicContent {
    @Field(type = FieldType.Keyword)
    private String id;
    @Field(index = false, type = FieldType.Keyword)
    private Long topicId;
    @Field(index = false, type = FieldType.Keyword)
    private Long sectionId;
    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String name;
    @Field(index = false, type = FieldType.Boolean)
    private Boolean status;
    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String data;

}
