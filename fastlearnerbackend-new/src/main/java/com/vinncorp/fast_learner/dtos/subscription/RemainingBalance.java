package com.vinncorp.fast_learner.dtos.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RemainingBalance {

    private double remainingBalance;
    private int trialOccurrences;

    public RemainingBalance(double remainingBalance, int noOfMonths) {
        this.remainingBalance = remainingBalance;
        this.trialOccurrences = noOfMonths;
    }
}
