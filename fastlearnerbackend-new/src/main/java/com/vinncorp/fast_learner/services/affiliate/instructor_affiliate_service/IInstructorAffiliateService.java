package com.vinncorp.fast_learner.services.affiliate.instructor_affiliate_service;

import com.vinncorp.fast_learner.dtos.affiliate.AffiliatePremiumCourse;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.affiliate.InstructorAffiliate;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IInstructorAffiliateService {
    InstructorAffiliate getByInstructorIdAndAffiliateId(Long instructorId, Long affiliateId) throws BadRequestException;
    Message<AffiliatePremiumCourse> getPremiumCoursesWithAffiliateReward(Long affiliateId, String name) throws EntityNotFoundException, BadRequestException;
    InstructorAffiliate getByAffiliateUUIDAndStatus(String affiliateUUID, GenericStatus status) throws BadRequestException;
}
