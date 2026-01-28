package com.vinncorp.fast_learner.dtos.purchase_course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCoursePurchased {

    private Long courseId;
    private String studentName;
    private String studentEmail;
    private String courseTitle;
    private Date enrolledAt;
    private Double price;
    private Double originalPrice;
    private Double discount;
}
