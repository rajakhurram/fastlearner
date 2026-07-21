package com.vinncorp.fast_learner.util.enums;

public enum CourseType {
    STANDARD_COURSE(0), PREMIUM_COURSE(1), FREE_COURSE(2);

    private int value;

    CourseType(int value){this.value = value;}

    public int getValue(){return this.value;}

    public static CourseType fromValue(int value){
        for(CourseType s: CourseType.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }

    public static boolean isValidCourseType(String courseType) {
        try {
            CourseType.valueOf(courseType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
