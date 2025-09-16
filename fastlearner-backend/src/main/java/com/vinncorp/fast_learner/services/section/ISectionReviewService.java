package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.request.section.CreateSectionReviewRequest;
import com.vinncorp.fast_learner.response.section.SectionReviewResponse;
import com.vinncorp.fast_learner.util.Message;

public interface ISectionReviewService {
    Message<String> createSectionReview(CreateSectionReviewRequest request, String email)
            throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<SectionReviewResponse> findBySectionId(Long sectionId, String email) throws BadRequestException, EntityNotFoundException;
}
