package com.vinncorp.fast_learner.services.payout;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;
import jakarta.persistence.Tuple;

import java.util.List;

public interface IPayoutWatchTimeService {
    Message<String> create(Long courseId, long watchTime, String email) throws EntityNotFoundException, BadRequestException, InternalServerException;

    List<Tuple> fetchPayoutForEachInstructor();

    void updatePayoutCalculationDate();
}
