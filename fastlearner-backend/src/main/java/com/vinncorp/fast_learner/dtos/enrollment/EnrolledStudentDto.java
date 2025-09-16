package com.vinncorp.fast_learner.dtos.enrollment;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledStudentDto {

    private long totalValue;
    private List<EnrolledStudentValue> values;

    public static EnrolledStudentDto from(List<Tuple> data) {
        List<EnrolledStudentValue> values = new ArrayList<>();
        for (Tuple t : data) {
            values.add(EnrolledStudentValue.builder()
                            .value((Long) t.get("total_students"))
                            .monthDate((Date) t.get("period"))
                    .build());
        }
        return EnrolledStudentDto.builder().totalValue((Long) data.get(0).get("total")).values(values).build();
    }
}
