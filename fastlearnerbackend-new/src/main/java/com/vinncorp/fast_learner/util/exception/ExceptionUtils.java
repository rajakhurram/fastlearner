package com.vinncorp.fast_learner.util.exception;

public class ExceptionUtils {

    public static <T> T safelyFetch(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}
