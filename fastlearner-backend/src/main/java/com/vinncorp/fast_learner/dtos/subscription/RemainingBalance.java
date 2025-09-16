package com.vinncorp.fast_learner.dtos.subscription;

import lombok.Getter;

@Getter
public class RemainingBalance {

    public enum TRIAL_PERIOD {DAY, MONTHLY, YEARLY};

    private double remainingBalance;
    private long noOfTrialDays;
    private TRIAL_PERIOD trialPeriod;


    public RemainingBalance(double remainingBalance, TRIAL_PERIOD trialPeriod) {
        this.remainingBalance = remainingBalance;
        this.noOfTrialDays = 0;
        this.trialPeriod = trialPeriod;
    }

    public RemainingBalance(double remainingBalance, long noOfTrialDays) {
        this.remainingBalance = remainingBalance;
        this.noOfTrialDays = noOfTrialDays;
        this.trialPeriod = TRIAL_PERIOD.DAY;
    }
}
