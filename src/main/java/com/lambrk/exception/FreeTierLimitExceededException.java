package com.lambrk.exception;

public class FreeTierLimitExceededException extends RuntimeException {

    public FreeTierLimitExceededException(String message) {
        super(message);
    }

    public FreeTierLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
