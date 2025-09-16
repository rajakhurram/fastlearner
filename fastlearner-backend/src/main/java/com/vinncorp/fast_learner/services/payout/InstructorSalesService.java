package com.vinncorp.fast_learner.services.payout;

import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.payout.InstructorSales;
import com.vinncorp.fast_learner.repositories.payout.InstructorSalesRepository;
import com.vinncorp.fast_learner.util.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorSalesService implements IInstructorSalesService{

    private final InstructorSalesRepository repo;

    @Override
    public void saveAll(List<InstructorSales> instructorSalesList) throws InternalServerException {
        log.info("Saving all instructor sales.");
        try {
            repo.saveAll(instructorSalesList);
        } catch (Exception e) {
            log.error("ERROR: "+ e.getLocalizedMessage());
            throw new InternalServerException("Instructor sales " + InternalServerException.NOT_SAVED_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<InstructorSales> findAllForPayoutProcess(PayoutStatus payoutStatus) throws EntityNotFoundException {
        log.info("Fetching all pending instructor's sales for payout process.");
        List<InstructorSales> sales = repo.findAllForPayoutProcess(payoutStatus);
        if(CollectionUtils.isEmpty(sales))
            throw new EntityNotFoundException("No instructor sales found for payout process.");
        return sales;
    }

    @Override
    public InstructorSales findUnprocessedInstructor() throws EntityNotFoundException {
        return repo.findUnprocessedInstructorsOfCurrentMonth().orElseThrow(() -> new EntityNotFoundException("No pending payout data found for any instructor."));
    }

    @Override
    public void updateStatusOfPayout(String batchId) {
        log.info("Updating the payout status...");
        repo.updatePendingToProcessedByBatchId(batchId);
        log.info("Updated the payout status.");
    }

    @Override
    public Double fetchMonthlyOrYearlySales(String period, Long instructorId) {
        log.info("Fetching sales by " + period.toLowerCase() + "...");
        Double totalSales = 0.0;
        if(period.equalsIgnoreCase("YEARLY") || period.equalsIgnoreCase("PREVIOUS_YEAR"))
            totalSales = repo.fetchSalesByYearly(period, instructorId);
        else if(period.equalsIgnoreCase("MONTHLY") || period.equalsIgnoreCase("PREVIOUS_MONTH"))
            totalSales = repo.fetchSalesByMonthly(period, instructorId);
        return totalSales;
    }

    @Override
    public void save(InstructorSales instructorSales) {
        repo.save(instructorSales);
    }
}
