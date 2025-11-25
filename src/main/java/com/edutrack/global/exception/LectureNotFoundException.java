package com.edutrack.global.exception;

public class LectureNotFoundException extends RuntimeException {

  public LectureNotFoundException(Long lectureId) {
    super("해당 강의를 찾을 수 없습니다. ID: " + lectureId);
  }
}
