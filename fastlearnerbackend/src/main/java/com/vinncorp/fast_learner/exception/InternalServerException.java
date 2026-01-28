package com.vinncorp.fast_learner.exception;

public class InternalServerException extends Exception{
    public static final String NOT_SAVED_INTERNAL_SERVER_ERROR = "cannot be saved due to database error.";
    public static final String NOT_DELETE_INTERNAL_SERVER_ERROR = "cannot be delete due to database error.";
    public InternalServerException() {
    }

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
