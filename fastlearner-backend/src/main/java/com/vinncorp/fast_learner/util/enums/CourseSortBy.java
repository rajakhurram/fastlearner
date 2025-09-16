package com.vinncorp.fast_learner.util.enums;

public enum CourseSortBy {
    RECENTLY_ACCESSED(0),OLDEST_ACCESSED(1), COMPLETED(2), IN_PROGRESS(3),VIEW_ALL(4);

    private int value;
    CourseSortBy(int value) {
        this.value = value;
    }

    public int getValue(){
        return  this.value;
    }
    public static CourseSortBy fromValue(int value){
        for(CourseSortBy s: CourseSortBy.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }
}
