package com.vinncorp.fast_learner.services.authorize_net.checkout;

import com.vinncorp.fast_learner.dtos.authorize_net.checkout.ChargePayment;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.util.Message;

public interface IAuthorizeNetCheckoutService {
    Message<String> chargePayment(ChargePayment chargePayment, String email) throws EntityNotFoundException, BadRequestException, InternalServerException;
}
