package com.vinncorp.fast_learner.dtos.section;

import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.services.enrollment.IEnrollmentService;
import com.vinncorp.fast_learner.util.enums.CourseType;
import jakarta.persistence.Tuple;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlternateSectionDetail {
    private Long courseId;
    private Long instructorId;
    private String instructorName;
    private String instructorImage;
    private int totalReviewer;
    private double totalReviews;
    private Long sectionId;
    private String sectionName;
    private Boolean isFree;
    private String courseTitle;
    private String courseUrl;
    private String courseType;


    public static List<AlternateSectionDetail> from(List<Tuple> content, SubscribedUser subscribedUser) {
        List<AlternateSectionDetail> alternateSectionDetails = new ArrayList<>();
        for(Tuple e: content){
            Boolean isFree = true;
            String courseType = (String) e.get("course_type");
            if (courseType.equalsIgnoreCase(CourseType.PREMIUM_COURSE.name()) && !Boolean.parseBoolean("" + e.get("is_enrolled"))) {
                isFree = false;
            }else if(courseType.equalsIgnoreCase(CourseType.STANDARD_COURSE.name())){
                isFree = Objects.nonNull(subscribedUser.getSubscribedId()) || Boolean.parseBoolean("" + e.get("is_free"));
            }

            alternateSectionDetails.add(AlternateSectionDetail.builder()
                        .courseId(Long.parseLong(e.get("course_id").toString()))
                        .sectionId(Long.parseLong(e.get("section_id").toString()))
                        .sectionName((String) e.get("section_name"))
                        .instructorId(Long.parseLong(e.get("instructor_id").toString()))
                        .instructorName((String) e.get("instructor_name"))
                        .instructorImage((String) e.get("profile_picture"))
                        .totalReviewer(e.get("total_reviews") != null ? Integer.parseInt("" + e.get("total_reviews")) : 0 )
                        .totalReviews(e.get("avg_section_rating") != null ? Double.parseDouble("" + e.get("avg_section_rating")) : 0)
                        .courseTitle((String) e.get("course_name"))
                        .courseUrl((String) e.get("course_url"))
                        .courseType(
                            courseType.equalsIgnoreCase(CourseType.PREMIUM_COURSE.name()) ? "PREMIUM" :
                            courseType.equalsIgnoreCase(CourseType.STANDARD_COURSE.name()) ? "STANDARD" :
                            "FREE"
                        )

                    .isFree(isFree)
                        .build());

        }
        return alternateSectionDetails;
    }
//        return content.stream()
//                .map(e -> AlternateSectionDetail.builder()
//                        .courseId(Long.parseLong(e.get("course_id").toString()))
//                        .sectionId(Long.parseLong(e.get("section_id").toString()))
//                        .sectionName((String) e.get("section_name"))
//                        .instructorId(Long.parseLong(e.get("instructor_id").toString()))
//                        .instructorName((String) e.get("instructor_name"))
//                        .instructorImage((String) e.get("profile_picture"))
//                        .totalReviewer(e.get("total_reviews") != null ? Integer.parseInt("" + e.get("total_reviews")) : 0 )
//                        .totalReviews(e.get("avg_section_rating") != null ? Double.parseDouble("" + e.get("avg_section_rating")) : 0)
//                        .courseTitle((String) e.get("course_name"))
//                        .courseUrl((String) e.get("course_url"))
//                        .courseType((String) e.get("course_type"))
//                        .isFree(!StringUtils.isEmpty(isPaid) || Boolean.parseBoolean("" + e.get("is_free")))
//                        .build())
//                .toList();
//    }
}
