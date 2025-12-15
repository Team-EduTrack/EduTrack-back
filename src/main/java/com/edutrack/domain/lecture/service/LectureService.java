package com.edutrack.domain.lecture.service;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureStudentAssignResponse;
import com.edutrack.domain.lecture.dto.StudentSearchResponse;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.service.LectureStatisticsService;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureService {

  private final LectureRepository lectureRepository;
  private final LectureStudentRepository lectureStudentRepository;
  private final UserRepository userRepository;
  private final ExamRepository examRepository;
  private final LectureStatisticsService lectureStatisticsService;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;

  private final LectureHelper lectureHelper;

  //강사의 ID로 강의 목록과 각 강의의 수강생 수 조회
  @Transactional(readOnly = true)
  public List<LectureForTeacherResponse> getLecturesByTeacherId(Long teacherId) {

    //강사의 강의 목록 조회
    List<Lecture> lectures = lectureRepository.findAllByTeacherId(teacherId);

    // Batch 조회를 통한 해당 강의들의 모든 수강생 수 조회
    List<LectureStudent> lectureStudents = findLectureStudentByLectureId(lectures);

    // 강의별 수강생수 조회
    Map<Long, Long> studentCountMap = lectureStudents.stream()
        .collect(Collectors.groupingBy(ls -> ls.getLecture().getId(), Collectors.counting()));

    //각 강의와 수강생 수를 매핑하여 DTO 생성
    return lectures.stream()
        .map(lecture -> {
          // 각 강의의 시험 목록 조회 (기존 메서드 재활용)
          List<Exam> exams = examRepository.findByLectureId(lecture.getId());
          // LectureStatisticsService의 calculateAverageScore 재사용
          Double averageScore = lectureStatisticsService.calculateAverageScore(exams);
          // 선생님 이름 조회
          String teacherName = lecture.getTeacher().getName();
          
          return LectureForTeacherResponse.of(
              lecture,
              studentCountMap.getOrDefault(lecture.getId(), 0L).intValue(),
              teacherName,
              averageScore
          );
        }).toList();
  }

  //강의 상세 조회 (선생용)
  @Transactional(readOnly = true)
  public LectureDetailForTeacherResponse getLectureDetailForTeacherId(Long lectureId, Long teacherId) {

    //강의 조회 및 권한 검증
    Lecture lecture = lectureHelper.getLectureWithValidation(lectureId, teacherId);

    //강의에 배정된 수강생 리스트 조회
    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);

    // 과제별 제출 학생 목록 조회 (제출자가 있는 과제만)
    List<LectureDetailForTeacherResponse.AssignmentWithSubmissions> assignmentsWithSubmissions = 
        getAssignmentsWithSubmissions(lectureId);

    return new LectureDetailForTeacherResponse(
        lecture.getId(),
        lecture.getTitle(),
        lecture.getDescription(),
        lectureStudents,
        null,  // teacherName은 목록 조회에서만 필요
        null,  // averageGrade는 목록 조회에서만 필요
        assignmentsWithSubmissions
    );
  }

  /**
   * 강의의 과제별 제출 학생 목록 조회
   * 제출자가 있는 과제만 반환 (0명인 과제는 제외)
   */
  private List<LectureDetailForTeacherResponse.AssignmentWithSubmissions> getAssignmentsWithSubmissions(Long lectureId) {
    // 강의의 모든 과제 조회
    List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);

    return assignments.stream()
        .map(assignment -> {
          // 해당 과제에 제출한 학생 목록 조회
          List<AssignmentSubmission> submissions = 
              assignmentSubmissionRepository.findAllByAssignmentId(assignment.getId());

          // 제출자가 있는 경우만 포함
          if (!submissions.isEmpty()) {
            List<LectureDetailForTeacherResponse.SubmissionStudentInfo> submittedStudents = 
                submissions.stream()
                    .map(submission -> new LectureDetailForTeacherResponse.SubmissionStudentInfo(
                        submission.getStudent().getId(),
                        submission.getStudent().getName(),
                        submission.getId()  // submissionId는 채점 페이지로 이동하기 위해 필요
                    ))
                    .toList();

            return new LectureDetailForTeacherResponse.AssignmentWithSubmissions(
                assignment.getId(),
                assignment.getTitle(),
                submittedStudents
            );
          }
          return null; // 제출자가 없으면 null
        })
        .filter(Objects::nonNull) // null 제거 (제출자가 없는 과제 제외)
        .toList();
  }

  //학생 목록 조회
  @Transactional(readOnly = true)
  public List<StudentSearchResponse> getStudentsByLecture(Long lectureId) {
    // LectureStudent 테이블에서 lectureId에 배정된 학생 조회
    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);

    // StudentSearchResponse로 매핑
    return lectureStudents.stream()
        .map(ls -> toDto(ls.getStudent()))
        .toList();
  }

  //배정 가능한 학생 조회
  @Transactional(readOnly = true)
  public List<StudentSearchResponse> getAvailableStudents(Long lectureId, String name) {

    Lecture lecture = lectureHelper.getLectureOrThrow(lectureId);

    Long academyId = lecture.getAcademy().getId();

    //이미 배정된 학생 ID 조회
    List<Long> assignedIds = lectureStudentRepository.findAllByLectureId((lectureId))
        .stream().map(ls -> ls.getStudent().getId())
        .toList();

    //배정 가능한 학생 조회
    List<User> candidates = userRepository.findAvailableStudents(
        academyId,
        assignedIds.isEmpty() ? Collections.emptyList() : assignedIds,
        name
    );

    //문자열 기반 필터링, name이 null 일 경우 모든 배정 가능한 학생 반환
    if(name != null && !name.isEmpty()) {
    String lowerName = name.toLowerCase();
    candidates = candidates.stream()
        .filter(u -> u.getName() != null && u.getName().toLowerCase().contains(lowerName))
        .toList();
    }

    return candidates.stream()
        .filter(lectureHelper::isStudent)
        .map(this::toDto)
      .toList();
  }

  //학생 배정 API
  @Transactional
  public LectureStudentAssignResponse assignStudents(Long lectureId, @NotEmpty List<Long> studentIds) {
    Lecture lecture = lectureHelper.getLectureOrThrow(lectureId);

    //학원 소속의 전체 학생 조회
    Set<User> students = lectureHelper.getValidStudents(studentIds, lecture);

    //이미 배정된 학생 ID 목록 조회
    Set<Long> assignedIds = lectureStudentRepository.findAllByLectureId(lectureId).stream()
        .map(ls -> ls.getStudent().getId())
        .collect(Collectors.toSet());

    //배정 가능한 학생 필터링
    Set<User> newStudents = students.stream()
        .filter(s -> !assignedIds.contains(s.getId()))
        .collect(Collectors.toSet());

    //newStudents가 비어있을 경우 saveAll 호출을 피하기 위한 안전장치
    if(newStudents.isEmpty()) {
      return new LectureStudentAssignResponse(lectureId, 0);
    }

    //LectureStudent 테이블에 배정 정보 저장
    Set<LectureStudent> lectureStudents = newStudents.stream()
        .map(s -> new LectureStudent(lecture, s))
        .collect(Collectors.toSet());


      lectureStudentRepository.saveAll(lectureStudents);

    return new LectureStudentAssignResponse(lectureId, lectureStudents.size());
  }

  private List<LectureStudent> findLectureStudentByLectureId(List<Lecture> lectures) {
    List<Long> ids = lectures.stream()
        .map(Lecture::getId)
        .filter(Objects::nonNull)
        .toList();
    return ids.isEmpty() ? Collections.emptyList() : lectureStudentRepository.findAllByLectureIdIn(ids);
  }

  private StudentSearchResponse toDto(User user) {
    return StudentSearchResponse.builder()
        .studentId(user.getId())
        .name(user.getName())
        .phone(user.getPhone())
        .build();
  }
}
