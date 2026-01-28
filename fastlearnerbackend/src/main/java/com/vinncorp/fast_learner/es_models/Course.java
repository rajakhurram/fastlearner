package com.vinncorp.fast_learner.es_models;

import com.vinncorp.fast_learner.util.es_indeces.Indices;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = Indices.COURSE_INDEX)
@Setting(settingPath = "static/es-settings.json")
public class Course {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Long)
    private Long dbId;

    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String courseUrl;

    @Field(type = FieldType.Text)
    private String docVector;

    @Field(type = FieldType.Text)
    private String courseStatus;
    @Field(type = FieldType.Text)
    private String thumbnail;
}
