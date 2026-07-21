package com.vinncorp.fast_learner.response.topic;

import com.vinncorp.fast_learner.dtos.topic.AlternativeTopicDetail;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AlternativeTopicResponse {

    private List<AlternativeTopicDetail> details;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;

}
