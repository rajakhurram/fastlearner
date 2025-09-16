package com.vinncorp.fast_learner.response.premium_student;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PremiumStudentResponse {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
    private Date  purchaseDate;// New field for purchase date

    public static Page<PremiumStudentResponse> toFrom(Page<Tuple> data){
        Page<PremiumStudentResponse> premiumStudents = data.map(tuple -> new PremiumStudentResponse(
                tuple.get("userId", Long.class),
                tuple.get("fullName", String.class),
                tuple.get("email", String.class),
                tuple.get("courseId", Long.class),
                tuple.get("courseTitle", String.class),
                tuple.get("enrolledDate", Date.class)
        ));
        return premiumStudents;
    }
}
