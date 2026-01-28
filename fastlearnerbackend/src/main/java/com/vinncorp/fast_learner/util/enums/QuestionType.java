package com.vinncorp.fast_learner.util.enums;

import lombok.Getter;

@Getter
public enum QuestionType {
    MULTIPLE_CHOICE(0), SINGLE_CHOICE(1), TRUE_FALSE(2), TEXT_FIELD(3);


    private int value;

    QuestionType(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static QuestionType fromValue(int value) {
        for (QuestionType quizQuestionType : QuestionType.values()) {
            if (quizQuestionType.value == value) {
                return quizQuestionType;
            }
        }
        return null;
    }
}
