package com.vinncorp.fast_learner.mock.otp;

import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.otp.Otp;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtpTestData {
    public static Otp otp() {
        return Otp.builder()
                .id(1L)
                .user(UserTestData.userData())
                .value(1111)
                .build();
    }
}
