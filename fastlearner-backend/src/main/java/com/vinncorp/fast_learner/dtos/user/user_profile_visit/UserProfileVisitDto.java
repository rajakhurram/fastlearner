package com.vinncorp.fast_learner.dtos.user.user_profile_visit;

import jakarta.persistence.Tuple;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVisitDto {

    private Long totalValue;
    private List<UserProfileVisitValue> values;

    public static UserProfileVisitDto from(List<Tuple> data) {
        long totalVisits = 0;
        List<UserProfileVisitValue> values = new ArrayList<>();
        for (Tuple t : data) {
            long visits = (Long) t.get("total_users");
            totalVisits += visits;
            values.add(UserProfileVisitValue.builder()
                    .monthDate((Date) t.get("period"))
                    .value((Long) t.get("total_users"))
                    .build());
        }

        return UserProfileVisitDto.builder()
                .values(values)
                .totalValue(totalVisits).build();
    }
}
