package com.vinncorp.fast_learner.services.subscription.process;

import com.vinncorp.fast_learner.dtos.payment.SubscriptionRequest;
import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.subscription.SubscribedUser;
import com.vinncorp.fast_learner.models.subscription.Subscription;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.Message;
import com.vinncorp.fast_learner.util.enums.SubscriptionProcessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SubscriptionContextService {

    @Autowired
    private Map<String, SubscriptionStrategyService> strategies;

    private SubscriptionStrategyService service;

    public void setProcess(SubscriptionProcessType processType) {
        switch (processType) {
            case CREATE -> service = strategies.get("create");
            case UPGRADE -> service = strategies.get("upgrade");
            case DOWNGRADE -> service = strategies.get("downgrade");
            case UPDATE -> service = strategies.get("update");
            case CANCEL -> service = strategies.get("cancel");
            case COUPON -> service = strategies.get("coupon");
        }
    }

    public Message<String> process(SubscriptionRequest requestDTO, SubscribedUser subscribedUser, Subscription nextSubscription, User user, Coupon coupon) throws BadRequestException, EntityNotFoundException, InternalServerException{
        if(service == null)
            throw new BadRequestException("Subscription strategy is not set.");
        return service.processSubscription(requestDTO, subscribedUser, nextSubscription, user, coupon);
    }
}
