package com.vinncorp.fast_learner.services.section;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.dtos.section.SectionDetail;
import com.vinncorp.fast_learner.response.section.SectionDetailForUpdateResponse;
import com.vinncorp.fast_learner.models.section.Section;
import com.vinncorp.fast_learner.response.section.SectionDetailResponse;
import com.vinncorp.fast_learner.util.Message;

import java.util.List;

public interface ISectionService {
    Section save(Section build) throws InternalServerException;

    List<SectionDetail> fetchSectionDetailByCourseId(Long courseId) throws EntityNotFoundException;

    Message<SectionDetailResponse> getAllSectionsByCourseId(Long courseId, String name) throws EntityNotFoundException, BadRequestException;

    Section findById(Long sectionId) throws EntityNotFoundException;

    Message<List<SectionDetailForUpdateResponse>> fetchAllSectionForUpdate(Long courseId, String email) throws EntityNotFoundException, BadRequestException;
    List<Section> getAllSectionsByCourseId(Long courseId);
}
