package com.vinncorp.fast_learner.response.message;

import lombok.Getter;


@Getter
public enum MessageTypeEnum {
    OK("Ok"), ERROR("Error");
    private String value;

    MessageTypeEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return  this.value;
    }

    public static MessageTypeEnum fromValue(String value){
        for(MessageTypeEnum s: MessageTypeEnum.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }
}
