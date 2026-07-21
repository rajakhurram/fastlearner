package com.vinncorp.fast_learner.dtos.payout;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaidUser {

    private long userId;
    private double subscriptionFee;

    public static List<PaidUser> map(List<Tuple> paidUsers) {
        return paidUsers.stream().map(e ->
                new PaidUser(Objects.isNull(e.get("user_id")) ? 0 : Long.parseLong("" + e.get("user_id")),
                        Objects.isNull(e.get("subscription_fee")) ? 0.0 : Double.parseDouble( "" + e.get("subscription_fee")))
                ).toList();
    }
}
