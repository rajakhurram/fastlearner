package com.vinncorp.fast_learner.response.topic;

import com.vinncorp.fast_learner.dtos.topic.TopicNotesDetail;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicNotesResponse {

    private List<TopicNotesDetail> topicNotes;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
