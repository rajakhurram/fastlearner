package com.vinncorp.fast_learner.services.payout;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;

import java.util.List;

public interface IInstructorSalesService {
    void saveAll(List<InstructorSales> instructorSalesList) throws InternalServerException;

    List<InstructorSales> findAllForPayoutProcess(PayoutStatus payoutStatus) throws EntityNotFoundException;

    InstructorSales findUnprocessedInstructor() throws EntityNotFoundException;

    void updateStatusOfPayout(String batchId);

    Double fetchMonthlyOrYearlySales(String period, Long instructorId);

    void save(InstructorSales instructorSales);

}
