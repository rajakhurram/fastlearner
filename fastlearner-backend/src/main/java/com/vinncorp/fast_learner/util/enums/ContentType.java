package com.vinncorp.fast_learner.util.enums;

public enum ContentType {
    COURSE(0), TEST(1);

    private int value;

    ContentType(int value){this.value = value;}

    public int getValue(){return this.value;}

    public static ContentType fromValue(int value){
        for(ContentType s: ContentType.values()){
            if(s.value == value){
                return  s;
            }
        }
        return  null;
    }

    public static boolean isValidContentType(String contentType) {
        try {
            ContentType.valueOf(contentType.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
