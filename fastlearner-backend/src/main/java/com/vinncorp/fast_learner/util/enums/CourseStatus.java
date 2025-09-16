package com.vinncorp.fast_learner.util.enums;

public enum CourseStatus {
    DRAFT(0), PUBLISHED(1), UNPUBLISHED(2), DELETE(3);

    private int value;

    CourseStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return  this.value;
    }

    public static CourseStatus fromValue(int value){
        for(CourseStatus s: CourseStatus.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }

    public static boolean isValidCourseStatus(String courseStatus) {
        try {
            CourseStatus.valueOf(courseStatus.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
