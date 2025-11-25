package com.edutrack.global.exception;

public class LectureAccessDeniedException extends RuntimeException {

  public LectureAccessDeniedException(Long lectureId, Long userId) {
    super("사용자 ID: " + userId + "는 강의 ID: " + lectureId + "에 대한 접근 권한이 없습니다.");
  }
}
