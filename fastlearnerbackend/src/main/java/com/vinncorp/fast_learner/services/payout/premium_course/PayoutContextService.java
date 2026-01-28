package com.vinncorp.fast_learner.services.payout.premium_course;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.models.coupon.Coupon;
import com.vinncorp.fast_learner.models.course.Course;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.util.enums.PayoutType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PayoutContextService {

    @Autowired
    private Map<String, PayoutStrategyService> payoutStrategies;

    private PayoutStrategyService payoutStrategy;

    public void setPayoutStrategy(PayoutType payoutType){
        switch (payoutType) {
            case SELF -> payoutStrategy = payoutStrategies.get("self");
            case DIRECT -> payoutStrategy = payoutStrategies.get("direct");
            case AFFILIATE -> payoutStrategy = payoutStrategies.get("affiliate");
        }
    }

    public void executePayout(Course course, User user, String transId, String uuid, Coupon coupon) throws BadRequestException, InternalServerException {
        if(payoutStrategy == null)
            throw new BadRequestException("Payout strategy is not set.");
        payoutStrategy.executePayout(course, user, transId,uuid, coupon);
    }
}
