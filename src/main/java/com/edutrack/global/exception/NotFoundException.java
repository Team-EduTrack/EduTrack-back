package com.edutrack.global.exception;


//DB에서 리소스(강사, 학원 등)를 찾지 못했을 때 발생하는 예외
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}