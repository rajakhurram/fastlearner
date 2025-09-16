package com.vinncorp.fast_learner.response.user_session;

import com.vinncorp.fast_learner.response.auth.TokenResponse;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserSessionResponse {
    private Long subscriptionId;
    private Long courseId;
    private String courseUrl;
    private Double coursePrice;
    private TokenResponse tokenResponse;
}
