package com.vinncorp.fast_learner.dtos.affiliate;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.*;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AffiliateCourseDto {
    private Long id;
    private Date assignDate;
    private Long students;
    private Double reward;
    private String url;
    private Long courseId;
    private Long instructorAffiliateId;
    private String courseTitle;
    private Double revenue;
    private Long affiliateId;
    private String affiliateName;
    private GenericStatus status;


    public AffiliateCourseDto(Long id, Date assignDate, Long students, Double reward, String url, Long courseId, Long instructorAffiliateId, String courseTitle, Double revenue,GenericStatus status) {
        this.id = id;
        this.assignDate = assignDate;
        this.students = students;
        this.reward = reward;
        this.url = url;
        this.courseId = courseId;
        this.instructorAffiliateId = instructorAffiliateId;
        this.courseTitle = courseTitle;
        this.revenue = revenue;
        this.status = status;
    }

    public AffiliateCourseDto(Long id, Date assignDate, Long students, Double reward, String url, Long courseId, Long instructorAffiliateId, String courseTitle, Double revenue, Long affiliateId, String affiliateName) {
        this.id = id;
        this.assignDate = assignDate;
        this.students = students;
        this.reward = reward;
        this.url = url;
        this.courseId = courseId;
        this.instructorAffiliateId = instructorAffiliateId;
        this.courseTitle = courseTitle;
        this.revenue = revenue;
        this.affiliateId = affiliateId;
        this.affiliateName = affiliateName;
    }

    public AffiliateCourseDto(Long id, Date assignDate, Long students, Double reward, String url, Long courseId, Long instructorAffiliateId, String courseTitle, Double revenue) {
        this.id = id;
        this.assignDate = assignDate;
        this.students = students;
        this.reward = reward;
        this.url = url;
        this.courseId = courseId;
        this.instructorAffiliateId = instructorAffiliateId;
        this.courseTitle = courseTitle;
        this.revenue = revenue;
    }
}
