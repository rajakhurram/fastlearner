package com.vinncorp.fast_learner.util.Constants;

import com.vinncorp.fast_learner.util.enums.ContentType;
import com.vinncorp.fast_learner.util.enums.NotificationType;

import javax.swing.plaf.nimbus.State;
import java.util.Objects;

public class NotificationConstant {

    public static final String CREATE_NEW_COURSE_CONTENT = "New course is \"%s\" coming out, please check it out.";
    public static final String UPDATE_COURSE_CONTENT = "New lectures/materials have been added to your course %s";

    public static final String ENROLLMENT_CONTENT = " Enrolled in your course \"%s\".";
    public static final String PROFILE_VISIT_CONTENT = " Visited your profile.";
    public static final String COURSE_FAVOURITE_CONTENT = " Has added your course. \"%s\" to favorites";
    public static final String COURSE_NOT_FAVOURITE_CONTENT = " Unmarked as favourite, your course \"%s\".";
    public static final String COURSE_SHARE_CONTENT = " Shared your course \"%s\".";
    public static final String COURSE_REVIEW_CONTENT = " New rating received on your course \"%s\".";
    public static final String COURSE_REVIEW_UPDATED_CONTENT = "Updated the rating on your course \"%s\".";

    public static final String COURSE_VISIT_CONTENT = "Visited your course \"%s\".";
    public static final String SECTION_REVIEW_CONTENT = "Rated a section of your course \"%s\".";
    public static final String SECTION_REVIEW_UPDATE_CONTENT = "Updated rating on a section of your course \"%s\".";
    public static final String QnA_CONTENT = "Asked a question on a topic of your course \"%s\".";

    public static final String ENROLLMENT_ACHIEVEMENT = "Congratulations! Your course %s";

    public static final String COURSE_VISIT_ACHIEVEMENT = "Your course %s";

    public static final String COURSE_COMPLETION_RATE = "Your course %s";

    public static final String COURSE_QnA_DISCUSSION = "A student has asked a question in your course %s";

    public static final String CERTIFICATION_COMPLETION = "%s";

    public static final String ENROLLMENT_CONFIRMATION = "You have successfully enrolled in %s";

    public static final String COURSE_COMPLETION = "Congratulations! You have completed the course %s";

    //this has been changed in order to store progress of youtube videos, where as before it was giving database error, in place of 50%, 50%% has been added..
    public static final String PROGRESS_UPDATE = "You're 50%% through the course %s";

    public static final String NEW_SUBSCRIPTION = "Your %s";

    public static final String EXCLUSIVE_COURSE_ACCESS = "As a %s";

    public static final String COURSE_MILESTONE_ACHIEVED = "Well done! You've completed %s";

    public static final String CERTIFICATE_AWARDED = "You've earned a certificate for completing %s";

    public static final String NOTIFY_LIKE_DISLIKED_REVIEW = "%s";

    public static final String QnA_REPLY = "%s";

    public static String getCreateNewCourseContent(String courseType){
        return "New " +courseType+ " is \"%s\" coming out, please check it out.";
    }

    public static String getUpdateCourseContent(String courseType){
        return "New lectures/materials have been added to your "+courseType+" %s";
    }

    public static String getEnrollmentContent(String courseType){
        return "Enrolled in your "+courseType+" \"%s\".";
    }

    public static String getCourseReviewContent(String courseType){
        return "New rating received on your "+courseType+" \"%s\".";
    }

    public static String getCourseReviewUpdateContent(String courseType){
        return "Updated the rating on your "+courseType+" \"%s\".";
    }

    public static String getCourseFavouriteContent(String courseType){
        return "Has added your "+courseType+" \"%s\" to favorites";
    }

    public static String getCourseNotFavouriteContent(String courseType){
        return "Has removed your "+courseType+" \"%s\" from favorites";
    }

    public static String getSectionReviewContent(String courseType){
        return "You have a new review on "+courseType+" \"%s\".";
    }

    public static String getSectionReviewUpdateContent(String courseType){
        return "Updated rating on a section of your "+courseType+" \"%s\".";
    }

    public static String getCourseShareContent(String courseType){
        return "Shared your "+courseType+" \"%s\".";
    }

    public static String getQnaContent(String courseType){
        return "Asked a question on a topic of your "+courseType+" \"%s\".";
    }

    public static String getEnrollmentAchievementContent(String courseType){
        return "Congratulations! Your "+courseType+" %s";
    }

    public static String getCourseVisitContent(String courseType){
        return "Visited your "+courseType+" \"%s\".";
    }

    public static String getCourseVisitAchievementContent(String courseType){
        return "Your "+courseType+" %s";
    }

    public static String getCourseCompletionRateContent(String courseType){
        return "Your "+courseType+" %s";
    }

    public static String getCOURSE_QnA_DISCUSSIONContent(String courseType){
        return "A student has asked a question in your "+courseType+" %s";
    }

    public static String getCourseCompletionContent(String courseType){
        return "Congratulations! You have completed the "+courseType+" %s";
    }

    public static String getProgressUpdateContent(String courseType){
        return "You're 50%% through the "+courseType+" %s";
    }

    public static String value(String username, String courseName, ContentType courseContentType, NotificationType notificationType) {
        String courseType = Objects.isNull(courseContentType) || courseContentType.equals(ContentType.COURSE) ? "course" : "test";
        switch (notificationType) {
            case NEW_COURSE -> {
                return String.format(getCreateNewCourseContent(courseType), courseName);
            }
            case COURSE_UPDATED -> {
                return String.format(getUpdateCourseContent(courseType), courseName);
            }
            case ENROLLMENT -> {
                return String.format(getEnrollmentContent(courseType), courseName);
            }
            case COURSE_REVIEW -> {
                return String.format(getCourseReviewContent(courseType),  courseName);
            }
            case COURSE_REVIEW_UPDATED -> {
                return String.format(getCourseReviewUpdateContent(courseType), courseName);
            }
            case PROFILE_VISIT -> {
                return String.format(PROFILE_VISIT_CONTENT, username);
            }
            case COURSE_FAVOURITE -> {
                return String.format(getCourseFavouriteContent(courseType),  courseName);
            }
            case COURSE_NOT_FAVOURITE -> {
                return String.format(getCourseNotFavouriteContent(courseType),  courseName);
            }
            case SECTION_REVIEW -> {
                return String.format(getSectionReviewContent(courseType),  courseName);
            }
            case SECTION_REVIEW_UPDATED -> {
                return String.format(getSectionReviewUpdateContent(courseType), courseName);
            }
            case COURSE_SHARE -> {
                return String.format(getCourseShareContent(courseType),  courseName);
            }
            case QnA -> {
                return String.format(getQnaContent(courseType),  courseName);
            }
            case ENROLLMENT_ACHIEVEMENT -> {
                return String.format(getEnrollmentAchievementContent(courseType), courseName);
            }
            case COURSE_VISIT_ACHIEVEMENT -> {
                return String.format(getCourseVisitAchievementContent(courseType), courseName);
            }
            case COURSE_COMPLETION_RATE -> {
                return String.format(getCourseCompletionRateContent(courseType), courseName);
            }
            case COURSE_QnA_DISCUSSION -> {
                return String.format(getCOURSE_QnA_DISCUSSIONContent(courseType), courseName);
            }
            case CERTIFICATION_COMPLETION -> {
                return String.format(CERTIFICATION_COMPLETION, courseName);
            }
            case ENROLLMENT_CONFIRMATION -> {
                return String.format(ENROLLMENT_CONFIRMATION, courseName);
            }
            case COURSE_COMPLETION -> {
                return String.format(getCourseCompletionContent(courseType), courseName);
            }
            case PROGRESS_UPDATE -> {
                return String.format(getProgressUpdateContent(courseType), courseName);
            }
            case NEW_SUBSCRIPTION -> {
                return String.format(NEW_SUBSCRIPTION, courseName);
            }
            case EXCLUSIVE_COURSE_ACCESS -> {
                return String.format(EXCLUSIVE_COURSE_ACCESS, courseName);
            }
            case COURSE_MILESTONE_ACHIEVED -> {
                return String.format(COURSE_MILESTONE_ACHIEVED, courseName);
            }
            case CERTIFICATE_AWARDED -> {
                return String.format(CERTIFICATE_AWARDED, courseName);
            }
            case NOTIFY_LIKE_DISLIKED_REVIEW -> {
                return String.format(NOTIFY_LIKE_DISLIKED_REVIEW, courseName);
            }
            case QnA_REPLY -> {
                return String.format(QnA_REPLY, courseName);
            }
            case COURSE_VISIT_CONTENT -> {
                return String.format(COURSE_VISIT_CONTENT, courseName);
            }
        }
        return null;
    }
}
