package com.vinncorp.fast_learner.util.exception;

@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
