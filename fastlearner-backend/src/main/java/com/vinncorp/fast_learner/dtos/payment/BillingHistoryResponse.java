package com.vinncorp.fast_learner.dtos.payment;


import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.response.subscription.ArbTransaction;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingHistoryResponse {

    private String transId;
    private Date date;
    private Double amount;
    private String description;
    private String subscriptionId;
    private String status;

    public static List<BillingHistoryResponse> mapToBillingHistoryList(GetSubscriptionResponse subscription, SubscribedUser user)
            throws EntityNotFoundException {
        if(Objects.isNull(subscription.getSubscription().getArbTransactions()))
            throw new EntityNotFoundException("No transactions present.");
        List<ArbTransaction> transactions = subscription.getSubscription().getArbTransactions().getArbTransaction();
        return transactions.stream().map(e -> BillingHistoryResponse.builder()
                    .transId(e.getTransId())
                    .description(e.getResponse())
                    .subscriptionId(user.getPaymentSubscriptionId())
                    .amount(user.getSubscription().getPrice())
                    .date(e.getSubmitTimeUTC().toGregorianCalendar().getTime())
                    .build()
        ).toList();
    }
}
