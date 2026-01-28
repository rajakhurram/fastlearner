package com.vinncorp.fast_learner.exception;

public class EntityNotUpdateException extends Exception{
    public EntityNotUpdateException() {
    }

    public EntityNotUpdateException(String message) {
        super(message);
    }

    public EntityNotUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

}
