package com.vinncorp.fast_learner.response.transaction_history;

import com.vinncorp.fast_learner.util.enums.GenericStatus;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryResponse {
    private Long id;
    private Date creationAt;
    private String name;
    private String responseText;
    private GenericStatus status;
    private Double subscriptionAmount;
}
