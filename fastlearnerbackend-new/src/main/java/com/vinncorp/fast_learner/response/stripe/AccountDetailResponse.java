package com.vinncorp.fast_learner.response.stripe;

import com.stripe.model.Account;
import com.stripe.model.BankAccount;
import com.vinncorp.fast_learner.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailResponse {

    private String stripeId;
    private String fullName;
    private double balance;
    private String bankName;

    public static AccountDetailResponse mappedTo(Account account, User user, double amount) {
        var bankDetail = ((BankAccount)account.getExternalAccounts().getData().get(0));

        return AccountDetailResponse.builder()
                .stripeId(account.getId())
                .fullName(bankDetail.getAccountHolderName() == null ? user.getFullName() : bankDetail.getAccountHolderName())
                .balance(amount)
                .bankName(bankDetail.getBankName())
                .build();
    }
}
