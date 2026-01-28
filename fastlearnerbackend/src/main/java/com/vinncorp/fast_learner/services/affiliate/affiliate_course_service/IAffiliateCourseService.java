package com.vinncorp.fast_learner.services.affiliate.affiliate_course_service;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseDto;
import com.vinncorp.fast_learner.dtos.affiliate.AffiliateCourseRequest;
import com.vinncorp.fast_learner.exception.*;
import com.vinncorp.fast_learner.models.affiliate.AffiliatedCourses;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface IAffiliateCourseService {
    Message<String> assignCourseToAffiliate(List<AffiliateCourseRequest> affiliateCourseRequests, String name) throws EntityNotFoundException, BadRequestException, InternalServerException, LimitExceedException;
    AffiliateCourseDto getByInstructorAffiliateIdAndCourseIdAndStatus(Long instructorAffiliateId, Long courseId, GenericStatus status) throws BadRequestException;
    Message<Page<AffiliateCourseDto>> getAllCoursesByAffiliate(Long affiliateId, GenericStatus status, Pageable pageable, String name) throws EntityNotFoundException, BadRequestException;
    AffiliatedCourses getById(Long id);
    Message<String> deleteAffiliateCourse(Long affiliateId, Long affiliateCourseId, String name) throws EntityNotFoundException, AuthenticationException, BadRequestException;
    Message<Page<AffiliateCourseDto>> getAllAffiliatesByCourse(Long courseId, String name, GenericStatus instructorAffiliateStatus, GenericStatus affiliateCourseStatus, Pageable pageable) throws EntityNotFoundException;
    Boolean saveStudentOnboardingDetails(String affiliateUUID, Long courseId) throws BadRequestException, EntityNotFoundException, InternalServerException;

    Message<String> updateAssignCourseActiveAndInActive(Long courseId, Long instructorAffiliateId, GenericStatus status, Principal principal) throws BadRequestException, EntityNotFoundException, LimitExceedException;
}
