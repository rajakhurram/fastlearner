package com.vinncorp.fast_learner.exception;


import java.util.Map;

public class CreateCourseValidationException extends Exception {
    private Map<String, String> errors;

    public CreateCourseValidationException() {
        super();
    }

    public CreateCourseValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public CreateCourseValidationException(String message, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
