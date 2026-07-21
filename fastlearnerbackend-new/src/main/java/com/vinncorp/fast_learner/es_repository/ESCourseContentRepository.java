package com.vinncorp.fast_learner.es_repository;

import com.vinncorp.fast_learner.es_models.CourseContent;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ESCourseContentRepository extends ElasticsearchRepository<CourseContent, String> {
    @Query("""
    {
      "query": {
        "bool": {
          "should": [
            {
              "multi_match": {
                "query": "?0",
                "fields": [
                  "courseTitle",
                  "courseOutcome",
                  "courseAbout",
                  "courseTags"
                ],
                "fuzziness": "AUTO"
              }
            },
            {
              "nested": {
                "path": "sections",
                "query": {
                  "bool": {
                    "should": [
                      {
                        "match": {
                          "sections.sectionName": {
                            "query": "?0",
                            "fuzziness": "AUTO"
                          }
                        }
                      },
                      {
                        "nested": {
                          "path": "sections.topics",
                          "query": {
                            "match": {
                              "sections.topics.topicName": {
                                "query": "?0",
                                "fuzziness": "AUTO"
                              }
                            }
                          },
                          "inner_hits": {
                            "_source": ["sections.topics.topicId", "sections.topics.topicName"],
                            "highlight": {
                              "fields": {
                                "sections.topics.topicName": {
                                  "pre_tags": ["<b>"],
                                  "post_tags": ["</b>"]
                                }
                              }
                            }
                          }
                        }
                      }
                    ]
                  }
                },
                "inner_hits": {
                  "_source": ["sections.sectionId", "sections.sectionName"],
                  "highlight": {
                    "fields": {
                      "sections.sectionName": {
                        "pre_tags": ["<b>"],
                        "post_tags": ["</b>"]
                      }
                    }
                  }
                }
              }
            }
          ]
        }
      },
      "highlight": {
        "fields": {
          "courseTitle": {
            "pre_tags": ["<b>"],
            "post_tags": ["</b>"]
          },
          "courseOutcome": {
            "pre_tags": ["<b>"],
            "post_tags": ["</b>"]
          },
          "courseAbout": {
            "pre_tags": ["<b>"],
            "post_tags": ["</b>"]
          },
          "courseTags": {
            "pre_tags": ["<b>"],
            "post_tags": ["</b>"]
          }
        }
      }
    }
""")
    List<CourseContent> searchCourses(String searchTerm);

}


