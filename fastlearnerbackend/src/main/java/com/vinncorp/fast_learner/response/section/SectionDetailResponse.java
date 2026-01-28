package com.vinncorp.fast_learner.response.section;

import com.vinncorp.fast_learner.dtos.section.SectionDetailForContent;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class SectionDetailResponse {

    private String title;
    private String category;
    private Long prevSectionId;
    private Long prevTopicId;
    private boolean hasCertificate;
    private String courseUrl;
    private List<SectionDetailForContent> sectionDetails;
}
