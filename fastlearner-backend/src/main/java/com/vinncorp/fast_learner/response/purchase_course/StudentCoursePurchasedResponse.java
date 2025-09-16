package com.vinncorp.fast_learner.response.purchase_course;

import com.vinncorp.fast_learner.dtos.purchase_course.StudentCoursePurchased;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCoursePurchasedResponse {
    private List<StudentCoursePurchased> purchasedCourses;
    private int pageNo;
    private int pageSize;
    private Long totalElements;
    private int totalPages;

    public static StudentCoursePurchasedResponse mapTo(Page<Tuple> data) {
        List<StudentCoursePurchased> studentCourses = data.getContent().stream().map(e -> StudentCoursePurchased.builder()
                .courseId((Long) e.get("course_id"))
                .courseTitle((String) e.get("title"))
                .price((Double) e.get("price"))
                .enrolledAt(e.get("enrolled_date", Date.class))
                .build()).toList();

        return StudentCoursePurchasedResponse.builder()
                .purchasedCourses(studentCourses)
                .pageNo(data.getNumber())
                .pageSize(data.getSize())
                .totalElements(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .build();
    }
}
