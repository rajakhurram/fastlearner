package com.vinncorp.fast_learner.response.payment_checkout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vinncorp.fast_learner.response.customer_profile.CustomerProfileIdType;
import com.vinncorp.fast_learner.response.subscription.NameAndAddressType;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {

    private String responseCode;
    private String rawResponseCode;
    private String authCode;
    private String avsResultCode;
    private String cvvResultCode;
    private String cavvResultCode;
    private String transId;
    private String refTransID;
    private String transHash;
    private String testRequest;
    private String accountNumber;
    private String entryMode;
    private String accountType;
    private String splitTenderId;
    private PrePaidCard prePaidCard;
    private Messages messages;
    private Errors errors;
    private NameAndAddressType shipTo;
    private SecureAcceptance secureAcceptance;
    private EmvResponse emvResponse;
    private String transHashSha2;
    private CustomerProfileIdType profile;
    private String networkTransId;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SecureAcceptance {
        private String secureAcceptanceUrl;
        private String payerID;
        private String payerEmail;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrePaidCard {
        private String requestedAmount;
        private String approvedAmount;
        private String balanceOnCard;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Messages {
        private List<Messages.Message> message;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String code;
            private String description;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Errors {
        private List<Errors.Error> error;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Error {
            private String errorCode;
            private String errorText;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmvResponse {
        private String tlvData;
    }
}
