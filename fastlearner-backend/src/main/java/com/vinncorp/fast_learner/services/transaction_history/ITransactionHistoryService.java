package com.vinncorp.fast_learner.services.transaction_history;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.transaction_history.TransactionHistory;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.response.transaction_history.TransactionHistoryResponse;
import com.vinncorp.fast_learner.util.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vinncorp.fast_learner.util.enums.GenericStatus;
import com.vinncorp.fast_learner.util.enums.SubscriptionStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ITransactionHistoryService {
    TransactionHistory findByAuthSubscriptionId(String paymentSubscriptionId) throws EntityNotFoundException;

    TransactionHistory findByLatestTransactionHistoryBySubsIdAndStatus(String paymentSubscriptionId, GenericStatus genericStatus) throws EntityNotFoundException;

    TransactionHistory findByLatestTransactionHistoryBySubsIdAndStatusAndSubscriptionStatus(String paymentSubscriptionId,
                                                                                            GenericStatus genericStatus,
                                                                                            SubscriptionStatus subscriptionStatus)
            throws EntityNotFoundException;

    TransactionHistory save(TransactionHistory transactionHistory);

    Message<Page<TransactionHistoryResponse>> fetchTransactionHistory(Pageable pageable, String email) throws EntityNotFoundException;

    Message<TransactionHistory> getByTransactionId(Long transactionId) throws EntityNotFoundException;

    List<TransactionHistory> fetchAllTransactionHistoryBy(long studentId) throws EntityNotFoundException;

    TransactionHistory fetchCurrentSubscription(Long studentId) throws EntityNotFoundException;

    ResponseEntity<byte[]> downloadInvoiceByTransactionId(Long transactionId) throws EntityNotFoundException;

    TransactionHistory findByLatestTransactionHistoryByUserIdAndStatus(Long id, GenericStatus active) throws EntityNotFoundException;

    TransactionHistory findById(Long oldTransactionId) throws EntityNotFoundException;

    TransactionHistory findByLatestUserAndSubscriptionStatus(Long id, SubscriptionStatus aContinue);

    void inactiveTransactionHistoryWhenCouponBasedSubscriptionApplied(String responseText, Subscription subscription, SubscribedUser subscribedUser, User user);
}
