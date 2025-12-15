package com.edutrack.domain.lecture.controller;

import com.edutrack.domain.statistics.service.LectureStatisticsService;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutrack.domain.lecture.dto.LectureCreationRequest;
import com.edutrack.domain.lecture.dto.LectureCreationResponse;
import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureDetailWithStatisticsResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureStatisticsResponse;
import com.edutrack.domain.lecture.dto.LectureStudentAssignRequest;
import com.edutrack.domain.lecture.dto.LectureStudentAssignResponse;
import com.edutrack.domain.lecture.dto.StudentSearchResponse;
import com.edutrack.domain.lecture.service.LectureCreationService;
import com.edutrack.domain.lecture.service.LectureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

  private final LectureService lectureService;
  private final LectureCreationService lectureCreationService;
  private final LectureStatisticsService lectureStatisticsService;

  // 강의 생성 API

  @PostMapping

  @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL')")
  public ResponseEntity<LectureCreationResponse> createLecture(
          @Valid @RequestBody LectureCreationRequest request,
          @AuthenticationPrincipal Long teacherId) {

    Long lectureId = lectureCreationService.createLecture(teacherId, request);

    LectureCreationResponse response = LectureCreationResponse.builder()
            .lectureId(lectureId)
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  //강의 목록 조회 (선생용)
  @PreAuthorize(("hasAnyRole('TEACHER', 'PRINCIPAL')"))
  @GetMapping
  public ResponseEntity<List<LectureForTeacherResponse>> getLecturesByTeacherId(@AuthenticationPrincipal Long teacherId) {
    List<LectureForTeacherResponse> lectures = lectureService.getLecturesByTeacherId(teacherId);
    return ResponseEntity.ok(lectures);
  }

  //강의 상세 조회 (선생용)
  @PreAuthorize(("hasAnyRole('TEACHER', 'PRINCIPAL')"))
  @GetMapping("/{lectureId}")
  public ResponseEntity<LectureDetailWithStatisticsResponse> getLectureDetailWithStatisticsForTeacherId(
      @PathVariable Long lectureId,
      @AuthenticationPrincipal Long teacherId) {

    /**
     * 강의 진행률, 과제 제출률, 출석률
     */
    LectureDetailForTeacherResponse detail = lectureService.getLectureDetailForTeacherId(lectureId, teacherId);
    LectureStatisticsResponse statistics = lectureStatisticsService.getLecutureStatistics(lectureId, teacherId);

    LectureDetailWithStatisticsResponse response = new LectureDetailWithStatisticsResponse(detail, statistics);
    return ResponseEntity.ok(response);
  }

  //강의를 듣는 학생 목록 조회 API
  @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
  @GetMapping("/{lectureId}/students")
  public ResponseEntity<List<StudentSearchResponse>> getStudentsByLecture(
      @PathVariable Long lectureId) {

    List<StudentSearchResponse> students = lectureService.getStudentsByLecture(lectureId);
    return ResponseEntity.ok(students);
  }

  //강의에 배정되지 않은 학생 검색 API
  @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
  @GetMapping("/{lectureId}/available-students")
  public ResponseEntity<List<StudentSearchResponse>> getAvailableStudents(
      @PathVariable Long lectureId,
      @RequestParam String name) {
  List<StudentSearchResponse> availableStudents = lectureService.getAvailableStudents(lectureId, name);
    return ResponseEntity.ok(availableStudents);
  }


  //학생 강의 배정 API
  @PreAuthorize("hasAnyRole('TEACHER', 'PRINCIPAL')")
  @PostMapping("/{lectureId}/students")
  public ResponseEntity<LectureStudentAssignResponse> assignStudents(
      @PathVariable Long lectureId,
      @RequestBody @Valid LectureStudentAssignRequest request) {

    LectureStudentAssignResponse response = lectureService.assignStudents(
        lectureId, request.getStudentIds()
    );
    return ResponseEntity.ok(response);
  }
}
