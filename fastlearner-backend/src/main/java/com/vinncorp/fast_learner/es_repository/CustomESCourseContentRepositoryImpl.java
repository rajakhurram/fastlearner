package com.vinncorp.fast_learner.es_repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.vinncorp.fast_learner.es_models.CourseContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class CustomESCourseContentRepositoryImpl implements CustomESCourseContentRepository{

    @Autowired
    private ElasticsearchClient client;

    @Override
    public SearchResponse searchCoursesWithHighlights(String searchQuery) {
        try {
            SearchRequest request = SearchRequest.of(builder -> builder
                    .index("course_content")
                    .query(q -> q
                            .bool(b -> b
                                    .should(s -> s.multiMatch(m -> m
                                            .query(searchQuery)
                                            .fields("course_title", "course_outcome", "course_about", "course_tags")
                                            .fuzziness("AUTO")))
                                    .should(s -> s.nested(n -> n
                                            .path("sections")
                                            .query(q1 -> q1
                                                    .bool(b1 -> b1
                                                            .should(m1 -> m1.match(m -> m.field("sections.section_name").query(searchQuery).fuzziness("AUTO")))
                                                            .should(n1 -> n1.nested(n2 -> n2
                                                                    .path("sections.topics")
                                                                    .query(m2 -> m2.match(m -> m.field("sections.topics.topic_name").query(searchQuery).fuzziness("AUTO")))
                                                            ))
                                                    )
                                            )
                                    ))
                            )
                    )
                    .highlight(h -> h
                            .fields("course_title", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                            .fields("course_outcome", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                            .fields("course_about", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                            .fields("course_tags", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                            .fields("sections.section_name", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                            .fields("sections.topics.topic_name", HighlightField.of(f -> f
                                    .preTags("<b>")
                                    .postTags("</b>")
                            ))
                    )

            );

            // Execute the search request
            return client.search(request, CourseContent.class);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CourseContent saveCourseContent(CourseContent courseContent) throws IOException {
        IndexResponse response = client.index(IndexRequest.of(i -> i
                .index("course_content")
                .id(courseContent.getId())
                .document(courseContent)
        ));
        System.out.println("Document saved with ID: " + response.id() + " in index: " + response.index());
        return courseContent;
    }

}
