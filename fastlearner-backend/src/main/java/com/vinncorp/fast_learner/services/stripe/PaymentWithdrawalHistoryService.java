package com.vinncorp.fast_learner.services.stripe;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.stripe.PaymentWithdrawalHistory;
import com.vinncorp.fast_learner.repositories.stripe.PaymentWithdrawalHistoryRepository;
import com.vinncorp.fast_learner.response.stripe.PaymentWithdrawalHistoryResponse;
import com.vinncorp.fast_learner.dtos.stripe.PaymentWithdrawalHistoryDTO;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWithdrawalHistoryService implements IPaymentWithdrawalHistoryService{

    private final IUserService userService;
    private final PaymentWithdrawalHistoryRepository repo;

    @Override
    public Message<PaymentWithdrawalHistoryResponse> fetchConnectedAccountHistory(String email, int pageNo, int pageSize) throws EntityNotFoundException {
        log.info("Fetching all history of the payment withdrawal by instructor.");
        User user = userService.findByEmail(email);
        var data =  repo.findByUserId(user.getId(), PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "withdrawalAt")));
        if(data == null || CollectionUtils.isEmpty(data.getContent()))
            throw new EntityNotFoundException("No payment withdrawal history data found.");
        var histories = data.stream().map(e -> PaymentWithdrawalHistoryDTO.builder()
                .id(e.getId())
                .amount(e.getAmount())
                .bankName(e.getBankName())
                .withdrawalAt(e.getWithdrawalAt())
                .build()).toList();

        var response = PaymentWithdrawalHistoryResponse.builder()
                .histories(histories)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .build();

        return new Message<PaymentWithdrawalHistoryResponse>()
                .setStatus(HttpStatus.OK.value())
                .setCode(HttpStatus.OK.name())
                .setMessage("Payment withdrawn history successfully fetched.")
                .setData(response);
    }

    @Override
    public void save(PaymentWithdrawalHistory paymentWithdrawalHistory) throws InternalServerException {
        log.info("Saving payment withdrawal history.");
        try {
            repo.save(paymentWithdrawalHistory);
        } catch (Exception e) {
            throw new InternalServerException("Payment withdrawal history " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }
}
