package com.vinncorp.fast_learner.dtos.section;

import com.vinncorp.fast_learner.models.topic.Topic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicDetail {
    private Long sectionId;
    private Long topicId;
    private String topicName;
    private String topicType;
    private Integer topicLevel;
    private int topicDuration;
    private Boolean delete;
    private Long videoId;
    private String videoFileName;
    private String videoUrl;
    private String videoTranscribe;
    private Long quizId;
    private String quizTitle;
    private List<QuizQuestionAnswers> quizQuestionAnwserList;
    private String article;

    public static TopicDetail toTopicDetails(Topic topic){
        return TopicDetail
                .builder()
                .topicId(topic.getId())
                .topicName(topic.getName())
                .build();
    }
}
