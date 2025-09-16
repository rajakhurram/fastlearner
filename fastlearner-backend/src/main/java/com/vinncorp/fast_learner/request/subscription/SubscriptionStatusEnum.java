package com.vinncorp.fast_learner.request.subscription;

import lombok.Getter;

import javax.xml.bind.annotation.XmlEnumValue;
@Getter
public enum SubscriptionStatusEnum {
    ACTIVE("active"),
    EXPIRED("expired"),
    SUSPENDED("suspended"),
    CANCELED("canceled"),
    TERMINATED("terminated");


    private String value;

    SubscriptionStatusEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return  this.value;
    }

    public static SubscriptionStatusEnum fromValue(String value){
        for(SubscriptionStatusEnum s: SubscriptionStatusEnum.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }
}
