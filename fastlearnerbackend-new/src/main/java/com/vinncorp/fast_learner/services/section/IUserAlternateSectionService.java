package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.section.UserAlternateSection;
import com.vinncorp.fast_learner.response.section.AlternateSectionResponse;
import com.vinncorp.fast_learner.util.Message;


public interface IUserAlternateSectionService {
    Message<AlternateSectionResponse> fetchAlternateSection(Long courseId, Long sectionId, int pageNo, int pageSize, String email)
            throws BadRequestException, EntityNotFoundException;

    Message<String> pinAlternateSection(long courseId, long sectionId, long fromSectionId, long fromCourseId, String email) throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<String> unpinAlternateSection(long courseId, long sectionId, String email) throws EntityNotFoundException, InternalServerException;
    UserAlternateSection findByCourseId(Long courseId, Long userId);
}
