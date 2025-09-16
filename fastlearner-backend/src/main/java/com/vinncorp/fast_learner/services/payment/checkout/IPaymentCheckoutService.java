package com.vinncorp.fast_learner.services.payment.checkout;

import com.vinncorp.fast_learner.dtos.payment.checkout.ChargePayment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;

public interface IPaymentCheckoutService {
    Message<String> chargePayment(ChargePayment chargePayment, String email) throws EntityNotFoundException, BadRequestException, InternalServerException;
}
