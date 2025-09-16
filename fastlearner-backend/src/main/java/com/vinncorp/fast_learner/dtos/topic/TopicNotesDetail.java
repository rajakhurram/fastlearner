package com.vinncorp.fast_learner.dtos.topic;

import com.vinncorp.fast_learner.models.topic.TopicNotes;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicNotesDetail {

    private Long topicNotesId;
    private String notes;
    private String time;
    private String topicName;
    private String sectionName;
    private Long topicId;

    public static List<TopicNotesDetail> from(List<TopicNotes> data) {
        return data.stream()
                .map( e -> TopicNotesDetail.builder()
                        .topicNotesId(e.getId())
                        .notes(e.getNote())
                        .time(e.getTime())
                        .topicName(e.getTopic().getName())
                        .sectionName(e.getTopic().getSection().getName())
                        .topicId(e.getTopic().getId())
                        .build())
                .toList();
    }
}
