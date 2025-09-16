package com.vinncorp.fast_learner.dtos.section;

import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.util.TimeUtil;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionDetailForContent {

    private Long sectionId;
    private String sectionName;
    private int sequence;
    private long totalTopics;
    private long totalTopicCompleted;
    private String sectionDuration;
    private boolean isFree;
    private double review;
    private int noOfReviewers;
    private boolean isAlternateSection;

    public static List<SectionDetailForContent> from(List<Tuple> sections, Long courseId, SubscribedUser subscribedUser) {
        return sections.stream().map(e -> SectionDetailForContent.builder()
                .sectionId((Long) e.get("id"))
                .sectionName((String) e.get("name"))
                .sequence(e.get("sequence_number") != null ? Integer.parseInt( "" + e.get("sequence_number")) : 0)
                .totalTopics(Long.parseLong("" + e.get("topics")))
                .totalTopicCompleted(Long.parseLong("" + e.get("completed")))
                .sectionDuration(TimeUtil.convertDurationToString(Integer.parseInt("" + e.get("duration"))))
                .isFree(isSectionFree(subscribedUser, (String) e.get("course_type"), Boolean.parseBoolean("" + e.get("is_enrolled")), Boolean.parseBoolean("" + e.get("is_free"))))
                .review(Double.parseDouble("" +  e.get("sec_reviews")))
                .noOfReviewers(e.get("total_sec_reviews") != null ? Integer.parseInt("" + e.get("total_sec_reviews")) : 0)
                .isAlternateSection(e.get("course_id") != null && !e.get("course_id").equals(courseId))
                .build()).toList();
    }

    public static Boolean isSectionFree(SubscribedUser subscribedUser, String courseType, Boolean isEnrolled, Boolean sectionFree){
        Boolean isFree = true;
        if (courseType.equalsIgnoreCase(CourseType.PREMIUM_COURSE.name()) && !isEnrolled) {
            isFree = false;
        }else if(courseType.equalsIgnoreCase(CourseType.STANDARD_COURSE.name())){
            isFree = Objects.nonNull(subscribedUser.getSubscribedId()) || sectionFree;
        }

        return isFree;
    }
}
