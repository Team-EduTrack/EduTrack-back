package com.edutrack.global.exception;

public class AssignmentAlreadySubmittedException extends RuntimeException {
    public AssignmentAlreadySubmittedException(String message) {
        super(message);
    }
}
