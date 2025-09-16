package com.vinncorp.fast_learner.response.customer_profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.request.subscription.CustomerAddressExType;
import com.vinncorp.fast_learner.response.subscription.CustomerPaymentProfileMaskedType;
import com.vinncorp.fast_learner.response.subscription.CustomerProfileExType;
import com.vinncorp.fast_learner.util.enums.CustomerProfileTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProfileMaskedType extends CustomerProfileExType {
    private List<CustomerPaymentProfileMaskedType> paymentProfiles;
    private List<CustomerAddressExType> shipToList;
    protected CustomerProfileTypeEnum profileType;
    public CustomerProfileMaskedType() {
    }

    public List<CustomerPaymentProfileMaskedType> getPaymentProfiles() {
        if (this.paymentProfiles == null) {
            this.paymentProfiles = new ArrayList();
        }

        return this.paymentProfiles;
    }

    public List<CustomerAddressExType> getShipToList() {
        if (this.shipToList == null) {
            this.shipToList = new ArrayList();
        }

        return this.shipToList;
    }

    public CustomerProfileTypeEnum getProfileType() {
        return this.profileType;
    }

    public void setProfileType(CustomerProfileTypeEnum value) {
        this.profileType = value;
    }
}
