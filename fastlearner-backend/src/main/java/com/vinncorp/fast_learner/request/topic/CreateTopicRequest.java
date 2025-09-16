package com.vinncorp.fast_learner.request.topic;

import com.vinncorp.fast_learner.request.video.CreateVideoRequest;
import com.vinncorp.fast_learner.request.article.CreateArticleRequest;
import com.vinncorp.fast_learner.request.quiz.CreateQuizRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTopicRequest {

    private Long id;
    private Boolean delete;
    private String title;
    private int level;
    private Long topicTypeId;
    private String topicTypeName; // for response
    private int duration;

    private CreateQuizRequest quiz;
    private CreateVideoRequest video;
    private CreateArticleRequest article;
}
