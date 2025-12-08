package com.edutrack.global.exception;

public class AlreadySubmittedException extends RuntimeException {
    public AlreadySubmittedException(String message) {
        super(message);
    }
}

