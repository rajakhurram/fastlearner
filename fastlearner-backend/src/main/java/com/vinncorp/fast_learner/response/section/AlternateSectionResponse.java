package com.vinncorp.fast_learner.response.section;

import com.vinncorp.fast_learner.dtos.section.AlternateSectionDetail;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlternateSectionResponse {
    private List<AlternateSectionDetail> details;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
