package com.edutrack.global.exception;

import com.edutrack.global.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("유효성 검증에 실패했습니다.");

    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .errorCode("G-001")
            .message(message)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .errorCode("G-002")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .errorCode("R-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .errorCode("G-003")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .errorCode("G-004")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .errorCode("U-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(LectureAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleLectureAccessDeniedException(LectureAccessDeniedException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .errorCode("L-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(AssignmentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAssignmentNotFoundException(AssignmentNotFoundException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .errorCode("A-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(AssignmentAlreadySubmittedException.class)
  public ResponseEntity<ErrorResponse> handleAssignmentAlreadySubmittedException(AssignmentAlreadySubmittedException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .errorCode("A-002")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(AssignmentPermissionException.class)
  public ResponseEntity<ErrorResponse> handleAssignmentPermissionException(AssignmentPermissionException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .errorCode("A-003")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(TeacherNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTeacherNotFoundException(TeacherNotFoundException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .errorCode("T-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(AcademyMismatchException.class)
  public ResponseEntity<ErrorResponse> handleAcademyMismatchException(AcademyMismatchException ex) {
    ErrorResponse response = ErrorResponse.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .errorCode("AC-001")
            .message(ex.getMessage())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }
}
