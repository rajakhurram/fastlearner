package com.vinncorp.fast_learner.response.stripe;

import com.vinncorp.fast_learner.dtos.stripe.PaymentWithdrawalHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWithdrawalHistoryResponse {

    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private List<PaymentWithdrawalHistoryDTO> histories;

}
