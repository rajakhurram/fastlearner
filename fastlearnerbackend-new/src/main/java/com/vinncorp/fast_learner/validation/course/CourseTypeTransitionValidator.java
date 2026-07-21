package com.vinncorp.fast_learner.validation.course;

import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.request.course.CreateCourseRequest;
import com.vinncorp.fast_learner.request.section.CreateSectionRequest;
import com.vinncorp.fast_learner.util.enums.CourseStatus;
import com.vinncorp.fast_learner.util.enums.CourseType;

import java.util.Objects;
import java.util.Optional;

public final class CourseTypeTransitionValidator {

    private CourseTypeTransitionValidator() {
    }

    public static CourseType resolveEffectiveCourseType(CreateCourseRequest request) {
        if (Objects.isNull(request.getCourseType())) {
            return null;
        }

        boolean allSectionsFree = Objects.nonNull(request.getSections())
                && !request.getSections().isEmpty()
                && request.getSections().stream().allMatch(CreateSectionRequest::getIsFree);

        CourseType requestedType = CourseType.valueOf(request.getCourseType());
        if (requestedType == CourseType.STANDARD_COURSE && allSectionsFree) {
            return CourseType.FREE_COURSE;
        }
        if (requestedType == CourseType.FREE_COURSE && !allSectionsFree) {
            return CourseType.STANDARD_COURSE;
        }
        return requestedType;
    }

    public static boolean isPremiumCourseEditBlocked(Course course) {
        return Objects.nonNull(course)
                && isPremiumType(course.getCourseType())
                && course.getCourseStatus() != CourseStatus.DRAFT;
    }

    public static Optional<String> validateTransition(Course existingCourse, CourseType requestedEffectiveType) {
        if (Objects.isNull(existingCourse) || Objects.isNull(requestedEffectiveType)) {
            return Optional.empty();
        }

        if (existingCourse.getCourseStatus() == CourseStatus.DRAFT) {
            return Optional.empty();
        }

        CourseType currentType = existingCourse.getCourseType();
        if (Objects.isNull(currentType)) {
            return Optional.empty();
        }

        boolean pricingLocked = Boolean.TRUE.equals(existingCourse.getPricingLocked());
        boolean conversionUsed = Boolean.TRUE.equals(existingCourse.getPremiumConversionUsed());

        if (isPremiumType(currentType) && !isPremiumType(requestedEffectiveType)) {
            return Optional.of(CourseTypeTransitionMessages.PREMIUM_TO_FREE_NOT_ALLOWED);
        }

        if (pricingLocked && isPremiumType(currentType) && !isPremiumType(requestedEffectiveType)) {
            return Optional.of(CourseTypeTransitionMessages.PRICING_LOCKED_TYPE_CHANGE);
        }

        if (!isPremiumType(currentType) && isPremiumType(requestedEffectiveType) && conversionUsed) {
            return Optional.of(CourseTypeTransitionMessages.CONVERSION_ALREADY_USED);
        }

        return Optional.empty();
    }

    public static boolean isPremiumType(CourseType courseType) {
        return CourseType.PREMIUM_COURSE == courseType;
    }

    public static boolean isFreeOrStandardType(CourseType courseType) {
        return CourseType.FREE_COURSE == courseType || CourseType.STANDARD_COURSE == courseType;
    }
}
