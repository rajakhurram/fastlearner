package com.vinncorp.fast_learner.dtos.authorize_net.invoice;


import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class SubscriptionDetail {
    private String name;
    private double price;
    private Date startDate;
    private Date endDate;
    private String status;

}
