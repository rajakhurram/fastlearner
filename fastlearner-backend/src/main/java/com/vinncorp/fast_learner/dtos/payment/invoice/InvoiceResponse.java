package com.vinncorp.fast_learner.dtos.payment.invoice;


import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.subscription.GetSubscriptionResponse;
import lombok.Builder;
import lombok.Data;
import com.vinncorp.fast_learner.response.subscription.GetTransactionDetailsResponse;

@Data
@Builder
public class InvoiceResponse {
    private String paymentId;
    private SubscriptionDetail planDetail;
    private PaymentProfile paymentMethod;
    private UserDetail customer;

    public static InvoiceResponse mapToInvoiceResponse(GetTransactionDetailsResponse transactionDetailsResponse, GetSubscriptionResponse subscriptionResponse, User user){
        return InvoiceResponse.builder()
                .paymentId(transactionDetailsResponse.getTransaction().getTransId())
                .planDetail(SubscriptionDetail.builder()
                        .name(subscriptionResponse.getSubscription().getName())
                        .price(subscriptionResponse.getSubscription().getAmount().doubleValue())
                        .startDate(subscriptionResponse.getSubscription().getPaymentSchedule().getStartDate().toGregorianCalendar().getTime())
                        .status(subscriptionResponse.getSubscription().getStatus().name())
                        .build())
                .paymentMethod(PaymentProfile.builder()
                        .cardNo(transactionDetailsResponse.getTransaction().getPayment().getCreditCard().getCardNumber())
                        .cardType(transactionDetailsResponse.getTransaction().getPayment().getCreditCard().getCardType())
                        .expiryDate(transactionDetailsResponse.getTransaction().getPayment().getCreditCard().getExpirationDate())
                        .method("Credit Card")
                        .firstName(transactionDetailsResponse.getTransaction().getBillTo().getFirstName())
                        .lastName(transactionDetailsResponse.getTransaction().getBillTo().getLastName())
                        .build())
                .customer(UserDetail.builder()
                        .id(user.getId())
                        .name(user.getFullName())
                        . email(user.getEmail())
                        .build())
                .build();
    }
}
