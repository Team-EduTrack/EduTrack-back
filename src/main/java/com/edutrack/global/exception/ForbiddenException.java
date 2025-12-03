package com.edutrack.global.exception;

//비즈니스 로직 상 권한 또는 소속이 맞지 않아 접근이 금지될 때 발생하는 예외
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}