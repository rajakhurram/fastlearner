package com.vinncorp.fast_learner.util.enums;

public enum ReportStatus {
    PENDING(0), READY(1), FAILED(2);


    private int value;

    ReportStatus(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static ReportStatus fromValue(int value) {
        for (ReportStatus reportStatus : ReportStatus.values()) {
            if (reportStatus.value == value) {
                return reportStatus;
            }
        }
        return null;
    }
}
