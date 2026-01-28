package com.vinncorp.fast_learner.response.customer_profile;

import com.vinncorp.fast_learner.response.subscription.ApiResponse;
import com.vinncorp.fast_learner.response.subscription.ArrayOfNumericString;
import com.vinncorp.fast_learner.response.subscription.ArrayOfString;
import lombok.Data;


@Data
public class CreateCustomerProfileResponse extends ApiResponse {
    private String customerProfileId;

    private ArrayOfNumericString customerPaymentProfileIdList;

    private ArrayOfNumericString customerShippingAddressIdList;

    private ArrayOfString validationDirectResponseList;
}

