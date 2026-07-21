package com.vinncorp.fast_learner.integration.section;

import com.vinncorp.fast_learner.request.section.CreateSectionReviewRequest;
import com.vinncorp.fast_learner.test_util.Constants;

public class SectionReviewIntegrationTestData {
    public static CreateSectionReviewRequest createSectionReviewRequest(){
        CreateSectionReviewRequest request = new CreateSectionReviewRequest();
        request.setCourseId(Constants.VALID_COURSE_ID);
        request.setSectionId(Constants.SECTION_ID);
        request.setComment(Constants.SECTION_COMMENT);
        request.setValue(5);
        return request;
    }
}
