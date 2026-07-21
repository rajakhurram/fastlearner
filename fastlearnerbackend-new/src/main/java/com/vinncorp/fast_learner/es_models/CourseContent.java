package com.vinncorp.fast_learner.es_models;

import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.es_indeces.Indices;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = Indices.COURSE_CONTENT)
public class CourseContent {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Long, analyzer = "my_custom_analyzer")
    private Long dbId;

    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String outcome;

    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String description;

    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private List<String> tags;
    @Field(index = false, type = FieldType.Keyword)
    private String courseUrl;
    @Field(index = false, type = FieldType.Long)
    private Long createdBy;
    @Field(type = FieldType.Text, analyzer = "my_custom_analyzer")
    private String creatorName;
    @Field(index = false, type = FieldType.Keyword)
    private String thumbnailUrl;
    @Field(index = false, type = FieldType.Keyword)
    private String userProfileUrl;
    @Field(index = false, type = FieldType.Keyword)
    private String userPictureUrl;
    @Field(index = false, type = FieldType.Keyword)
    private String status;
    @Field(type = FieldType.Nested)
    private List<SectionContent> sections;

}
