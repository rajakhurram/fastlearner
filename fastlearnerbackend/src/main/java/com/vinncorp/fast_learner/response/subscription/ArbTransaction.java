package com.vinncorp.fast_learner.response.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.xml.datatype.XMLGregorianCalendar;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArbTransaction {
    protected String transId;
    protected String response;
    protected XMLGregorianCalendar submitTimeUTC;
    protected Integer payNum;
    protected Integer attemptNum;
}
