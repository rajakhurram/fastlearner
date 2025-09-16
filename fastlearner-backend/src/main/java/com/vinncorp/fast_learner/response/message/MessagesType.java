package com.vinncorp.fast_learner.response.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

@Data
public class MessagesType {
    protected MessageTypeEnum resultCode;
    protected List<Message> message;

}
