package com.edutrack.domain.lecture.service;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureStudentAssignResponse;
import com.edutrack.domain.lecture.dto.StudentSearchResponse;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.service.LectureStatisticsService;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.UserNotFoundException;

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
  private final ExamStudentRepository examStudentRepository;

  private final LectureHelper lectureHelper;

  //강사 또는 원장의 ID로 강의 목록과 각 강의의 수강생 수 조회
  @Transactional(readOnly = true)
  public List<LectureForTeacherResponse> getLecturesByTeacherId(Long userId) {
    // 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID=" + userId));

    // 역할에 따라 강의 목록 조회
    List<Lecture> lectures;
    if (user.hasRole(RoleType.TEACHER)) {
      // 강사인 경우: 자신이 담당하는 강의만 조회
      lectures = lectureRepository.findAllByTeacherId(userId);
    } else if (user.hasRole(RoleType.PRINCIPAL)) {
      // 원장인 경우: 자신의 학원에 속한 모든 강의 조회
      lectures = lectureRepository.findAllByAcademyId(user.getAcademy().getId());
    } else {
      // 다른 역할은 빈 리스트 반환
      return List.of();
    }

    if (lectures.isEmpty()) {
      return List.of();
    }

    // Batch 조회를 통한 해당 강의들의 모든 수강생 수 조회
    List<LectureStudent> lectureStudents = findLectureStudentByLectureId(lectures);

    // 강의별 수강생수 조회
    Map<Long, Long> studentCountMap = lectureStudents.stream()
        .collect(Collectors.groupingBy(ls -> ls.getLecture().getId(), Collectors.counting()));

    // 성능 최적화: 모든 강의의 시험을 한 번에 배치 조회
    List<Long> lectureIds = toIdList(lectures, Lecture::getId);
    
    List<Exam> allExams = lectureIds.isEmpty() ? List.of() : examRepository.findByLectureIdIn(lectureIds);
    
    // 강의별 시험 그룹핑
    Map<Long, List<Exam>> examsByLectureId = allExams.stream()
        .collect(Collectors.groupingBy(exam -> exam.getLecture().getId()));

    //각 강의와 수강생 수를 매핑하여 DTO 생성
    return lectures.stream()
        .map(lecture -> {
          // 그룹핑된 시험 목록 조회
          List<Exam> exams = examsByLectureId.getOrDefault(lecture.getId(), List.of());
          // LectureStatisticsService의 calculateAverageScore 재사용
          Double averageScore = lectureStatisticsService.calculateAverageScore(exams);
          // 선생님 이름 조회
          String teacherName = lecture.getTeacher().getName();
          
          return LectureForTeacherResponse.of(
              lecture,
              (int) (long) studentCountMap.getOrDefault(lecture.getId(), 0L),
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

    // 시험별 응시 현황 조회
    List<LectureDetailForTeacherResponse.ExamParticipationInfo> examsWithParticipation = 
        getExamsWithParticipation(lectureId, lectureStudents.size());

    return new LectureDetailForTeacherResponse(
        lecture.getId(),
        lecture.getTitle(),
        lecture.getDescription(),
        lectureStudents,
        null,  // teacherName은 목록 조회에서만 필요
        null,  // averageGrade는 목록 조회에서만 필요
        assignmentsWithSubmissions,
        examsWithParticipation
    );
  }

  /**
   * 강의의 과제별 제출 학생 목록 조회
   * 제출자가 있는 과제만 반환 (0명인 과제는 제외)
   */
  private List<LectureDetailForTeacherResponse.AssignmentWithSubmissions> getAssignmentsWithSubmissions(Long lectureId) {
    // 강의의 모든 과제 조회
    List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);

    if (assignments.isEmpty()) {
      return List.of();
    }

    // 성능 최적화: 모든 과제의 제출물을 한 번에 배치 조회
    List<Long> assignmentIds = toIdList(assignments, Assignment::getId);

    List<AssignmentSubmission> allSubmissions = assignmentSubmissionRepository.findAllByAssignmentIds(assignmentIds);

    // 과제별로 제출물 그룹핑
    Map<Long, List<AssignmentSubmission>> submissionsByAssignmentId = allSubmissions.stream()
        .collect(Collectors.groupingBy(submission -> submission.getAssignment().getId()));

    // DTO 생성 (제출자가 있는 과제만 포함)
    return assignments.stream()
        .map(assignment -> {
          List<AssignmentSubmission> submissions = submissionsByAssignmentId.getOrDefault(assignment.getId(), List.of());

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

  /**
   * 강의의 시험별 응시 현황 조회
   * 모든 시험을 포함하며, 각 시험별 응시한 학생 수와 전체 학생 수를 반환
   */
  private List<LectureDetailForTeacherResponse.ExamParticipationInfo> getExamsWithParticipation(
      Long lectureId, int totalStudentCount) {
    // 강의의 모든 시험 조회
    List<Exam> exams = examRepository.findByLectureId(lectureId);

    if (exams.isEmpty()) {
      return List.of();
    }

    // 성능 최적화: 모든 시험 ID를 한 번에 조회
    List<Long> examIds = toIdList(exams, Exam::getId);

    List<ExamStudent> allExamStudents = examStudentRepository.findAllByExamIds(examIds);

    // 시험별로 응시 학생 수 그룹핑
    Map<Long, Long> participationCountMap = allExamStudents.stream()
        .collect(Collectors.groupingBy(
            es -> es.getExam().getId(),
            Collectors.counting()
        ));

    // DTO 생성
    return exams.stream()
        .map(exam -> {
          long participatedCount = participationCountMap.getOrDefault(exam.getId(), 0L);
          return new LectureDetailForTeacherResponse.ExamParticipationInfo(
              exam.getId(),
              exam.getTitle(),
              (int) participatedCount,
              totalStudentCount
          );
        })
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
    List<Long> assignedIds = getAssignedStudentIds(lectureId);

    //배정 가능한 학생 조회 (Repository에서 이미 name과 STUDENT 역할로 필터링 처리됨)
    List<User> candidates = userRepository.findAvailableStudents(
        academyId,
        assignedIds,
        name
    );

    return candidates.stream()
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
    Set<Long> assignedIds = getAssignedStudentIdSet(lectureId);

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
    List<Long> ids = toIdList(lectures, Lecture::getId);
    return ids.isEmpty() ? Collections.emptyList() : lectureStudentRepository.findAllByLectureIdIn(ids);
  }

  private <T> List<Long> toIdList(List<T> entities, Function<T, Long> idExtractor) {
    return entities.stream()
        .map(idExtractor)
        .filter(Objects::nonNull)
        .toList();
  }

  private List<Long> getAssignedStudentIds(Long lectureId) {
    return lectureStudentRepository.findAllByLectureId(lectureId).stream()
        .map(ls -> ls.getStudent().getId())
        .toList();
  }

  private Set<Long> getAssignedStudentIdSet(Long lectureId) {
    return lectureStudentRepository.findAllByLectureId(lectureId).stream()
        .map(ls -> ls.getStudent().getId())
        .collect(Collectors.toSet());
  }

  private StudentSearchResponse toDto(User user) {
    return StudentSearchResponse.builder()
        .studentId(user.getId())
        .name(user.getName())
        .phone(user.getPhone())
        .build();
  }
}
