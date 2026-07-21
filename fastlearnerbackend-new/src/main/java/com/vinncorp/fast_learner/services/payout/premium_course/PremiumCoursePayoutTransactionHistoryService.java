package com.vinncorp.fast_learner.services.payout.premium_course;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.premium_course.PremiumCoursePayoutTransactionHistory;
import com.vinncorp.fast_learner.repositories.payout.premium_course.PremiumCoursePayoutTransactionHistoryRepository;
import com.vinncorp.fast_learner.response.payout.PremiumCoursePayoutTransactionHistoryResponse;
import com.vinncorp.fast_learner.util.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumCoursePayoutTransactionHistoryService implements IPremiumCoursePayoutTransactionHistoryService{

    private final PremiumCoursePayoutTransactionHistoryRepository repo;

    @Override
    public PremiumCoursePayoutTransactionHistory save(PremiumCoursePayoutTransactionHistory premiumCoursePayoutHistory) throws InternalServerException {
        log.info("Saving the premium course payout transaction history...");
        try {
            return repo.save(premiumCoursePayoutHistory);
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
            throw new InternalServerException("Premium course payout history cannot be saved in db.");
        }
    }

    @Override
    public Message<PremiumCoursePayoutTransactionHistoryResponse> fetchAllPremiumCoursePayoutTransactionHistoryForInstructor(int pageNo, int pageSize, String email) throws EntityNotFoundException {
        log.info("Fetching all premium course payout transaction history.");
        Page<PremiumCoursePayoutTransactionHistory> payoutHistory = repo.findAllByCourse_Instructor_EmailOrderByCreationDateDesc(email.trim().toLowerCase(), PageRequest.of(pageNo, pageSize));
        if (payoutHistory.isEmpty())
            throw new EntityNotFoundException("No data found for logged in user.");
        var mappedData = PremiumCoursePayoutTransactionHistoryResponse.builder()
                .payoutHistories(payoutHistory.getContent())
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(payoutHistory.getTotalPages())
                .totalElements(payoutHistory.getTotalElements())
                .build();
        return new Message<PremiumCoursePayoutTransactionHistoryResponse>()
                .setData(mappedData)
                .setStatus(HttpStatus.OK.value())
                .setMessage("Fetched all premium course payout transaction history.")
                .setCode(HttpStatus.OK.name());
    }
}
