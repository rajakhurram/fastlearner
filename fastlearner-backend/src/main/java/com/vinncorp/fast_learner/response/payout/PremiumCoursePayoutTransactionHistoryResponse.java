package com.vinncorp.fast_learner.response.payout;

import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCoursePayoutTransactionHistoryResponse {

    private List<PremiumCoursePayoutTransactionHistory> payoutHistories;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
}
