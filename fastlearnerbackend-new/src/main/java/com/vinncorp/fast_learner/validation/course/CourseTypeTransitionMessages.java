package com.vinncorp.fast_learner.validation.course;

public final class CourseTypeTransitionMessages {

    public static final String PREMIUM_TO_FREE_NOT_ALLOWED =
            "A premium course cannot be changed to a free or standard course. Pricing is locked once a course is premium.";

    public static final String CONVERSION_ALREADY_USED =
            "This course has already been converted to premium. Free-to-premium conversion is allowed only once.";

    public static final String PRICING_LOCKED_TYPE_CHANGE =
            "Course type cannot be changed because pricing is locked for this premium course.";

    public static final String PREMIUM_COURSE_NOT_EDITABLE =
            "Premium courses cannot be edited.";

    private CourseTypeTransitionMessages() {
    }
}
