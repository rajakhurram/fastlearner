package com.vinncorp.fast_learner.request.subscription;

import com.vinncorp.fast_learner.response.message.MessageTypeEnum;
import lombok.Getter;

import javax.xml.bind.annotation.XmlEnumValue;
@Getter
public enum SubscriptionUnitEnum {
    DAYS("days"),
    MONTHS("months");

    private String value;

    SubscriptionUnitEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return  this.value;
    }

    public static SubscriptionUnitEnum fromValue(String value){
        for(SubscriptionUnitEnum s: SubscriptionUnitEnum.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }
}
