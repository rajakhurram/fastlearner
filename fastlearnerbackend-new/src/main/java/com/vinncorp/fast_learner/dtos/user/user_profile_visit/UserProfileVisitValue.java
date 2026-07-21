package com.vinncorp.fast_learner.dtos.user.user_profile_visit;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVisitValue {
    private Date monthDate;
    private Long value;
}
