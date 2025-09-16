package com.vinncorp.fast_learner.mock.payment.checkout;

import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;

public class PaymentCheckoutTestData {

    public static ChargePayment chargePayment() {
        return ChargePayment.builder()
                .opaqueData("eyJjb2RlIjoiNTBfMl8wNjAwMDUzMEFBQTJCQjc5MkQwMzE2N0JCMDMyMjM3RTIxMTRFMTgxNjg0MUExMTQzQjRDRUNGNTUxMTlBNjI5Q0VFRjc4NEIxQjI4Mjc1NDM4NkZFMzlGN0VBN0FENzVBMjBBMzgwIiwidG9rZW4iOiI5NzMwMzgzMjIzNDk4NzQyNDAzNjAxIiwidiI6IjEuMSJ9")
                .courseId(29)
                .build();
    }
}
