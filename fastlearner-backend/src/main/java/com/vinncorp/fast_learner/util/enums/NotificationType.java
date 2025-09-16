package com.vinncorp.fast_learner.util.enums;

public enum NotificationType {

    ENROLLMENT(0), COURSE_REVIEW(1), PROFILE_VISIT(2),
    COURSE_FAVOURITE(3), SECTION_REVIEW(4), COURSE_SHARE(5),
    QnA(6), NEW_COURSE(7), COURSE_UPDATED(8), PAYPAL_SUBSCRIPTION_CREATED(9),
    PAYPAL_SUBSCRIPTION_ACTIVATED(10), COURSE_NOT_FAVOURITE(11), COURSE_REVIEW_UPDATED(12),
    SECTION_REVIEW_UPDATED(13), PAYPAL_SUBSCRIPTION_EXPIRED(14), PAYPAL_SUBSCRIPTION_SUSPENDED(15),
    PAYPAL_SUBSCRIPTION_CANCELLED(16), PAYPAL_SUBSCRIPTION_PAYMENT_FAILED(17),
    PAYPAL_SUBSCRIPTION_ACCOUNT_CLOSED(18), ENROLLMENT_ACHIEVEMENT(19), COURSE_VISIT_ACHIEVEMENT(20),
    COURSE_COMPLETION_RATE(21), COURSE_QnA_DISCUSSION(22), CERTIFICATION_COMPLETION(23),
    ENROLLMENT_CONFIRMATION(24), COURSE_COMPLETION(25), PROGRESS_UPDATE(26), NEW_SUBSCRIPTION(27),
    EXCLUSIVE_COURSE_ACCESS(28), COURSE_MILESTONE_ACHIEVED(29), CERTIFICATE_AWARDED(30), NOTIFY_LIKE_DISLIKED_REVIEW(31),
    QnA_REPLY(32), COURSE_VISIT_CONTENT(33);

    private int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static NotificationType fromValue(int value) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.value == value) {
                return notificationType;
            }
        }
        return null;
    }
}
