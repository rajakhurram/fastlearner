package com.vinncorp.fast_learner.services.payout.premium_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.response.payout.PremiumCoursePayoutTransactionHistoryResponse;
import com.vinncorp.fast_learner.util.Message;

public interface IPremiumCoursePayoutTransactionHistoryService {
    PremiumCoursePayoutTransactionHistory save(PremiumCoursePayoutTransactionHistory premiumCoursePayoutHistory) throws InternalServerException;

    Message<PremiumCoursePayoutTransactionHistoryResponse> fetchAllPremiumCoursePayoutTransactionHistoryForInstructor(int pageNo, int pageSize, String email) throws EntityNotFoundException;
}
