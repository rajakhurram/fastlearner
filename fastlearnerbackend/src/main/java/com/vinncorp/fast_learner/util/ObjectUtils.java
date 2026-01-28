package com.vinncorp.fast_learner.util;

import java.util.Arrays;

public class ObjectUtils {

    public static boolean areAllFieldsNull(Object obj) {
        if (obj == null) {
            return true;
        }
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .allMatch(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(obj) == null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field: " + field.getName(), e);
                    }
                });
    }
}
