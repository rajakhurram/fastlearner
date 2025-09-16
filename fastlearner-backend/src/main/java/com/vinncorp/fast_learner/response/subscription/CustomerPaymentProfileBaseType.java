package com.vinncorp.fast_learner.response.subscription;

import com.vinncorp.fast_learner.util.enums.CustomerTypeEnum;
import lombok.Data;

@Data
public class CustomerPaymentProfileBaseType {
    protected CustomerTypeEnum customerType;
    protected CustomerAddressType billTo;
}
