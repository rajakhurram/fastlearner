package com.vinncorp.fast_learner.services.stripe;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.stripe.PaymentWithdrawalHistory;
import com.vinncorp.fast_learner.response.stripe.PaymentWithdrawalHistoryResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IPaymentWithdrawalHistoryService {
    Message<PaymentWithdrawalHistoryResponse> fetchConnectedAccountHistory(String email, int pageNo, int pageSize) throws EntityNotFoundException;

    void save(PaymentWithdrawalHistory paymentWithdrawalHistory) throws InternalServerException;
}
