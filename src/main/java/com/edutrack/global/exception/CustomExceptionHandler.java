package com.edutrack.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(LectureNotFoundException.class)
  public ResponseEntity<String> handleLectureNotFoundException(LectureNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(AlreadySubmittedException.class)
  public ResponseEntity<String> handleAlreadySubmittedException(AlreadySubmittedException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(ExamDeadlineExceededException.class)
  public ResponseEntity<String> handleExamDeadlineExceededException(ExamDeadlineExceededException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(ExamClosedException.class)
  public ResponseEntity<String> handleExamClosedException(ExamClosedException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }
}
