package com.vinncorp.fast_learner.request.subscription;

import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;
@Data
public class PaymentScheduleType {
    private XMLGregorianCalendar startDate;
    private Short totalOccurrences;
    private Short trialOccurrences;
    private Interval interval;

    @Data
    public static class Interval {
        protected short length;
        protected SubscriptionUnitEnum unit;
    }
}
