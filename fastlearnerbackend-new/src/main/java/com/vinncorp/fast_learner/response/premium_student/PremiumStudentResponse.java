package com.vinncorp.fast_learner.response.premium_student;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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
    private Date purchaseDate;

    private Long sectionId;
    private String sectionTitle;
    private Long quizId;
    private String quizTitle;
    private String thumbnail;
    private String picture;

    public static Page<PremiumStudentResponse> toFrom(Page<Tuple> data) {
        return data.map(tuple -> PremiumStudentResponse.builder()
                .studentId(tuple.get("userId", Long.class))
                .studentName(tuple.get("fullName", String.class))
                .studentEmail(tuple.get("email", String.class))

                .courseId(tuple.get("courseId", Long.class))
                .courseTitle(tuple.get("courseTitle", String.class))
                .purchaseDate(tuple.get("enrolledDate", Date.class))

                .sectionId(tuple.get("sectionId", Long.class))
                .sectionTitle(tuple.get("sectionTitle", String.class))

                .quizId(tuple.get("quizId", Long.class))
                .quizTitle(tuple.get("quizTitle", String.class))
                .thumbnail(tuple.get("thumbnail",String.class))
                .picture(tuple.get("picture",String.class))

                .build());
    }
}
