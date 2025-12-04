package com.edutrack.global.exception;

public class ExamDeadlineExceededException extends RuntimeException {
    public ExamDeadlineExceededException(String message) {
        super(message);
    }
}

