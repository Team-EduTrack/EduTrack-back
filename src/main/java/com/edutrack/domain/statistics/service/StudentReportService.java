package com.edutrack.domain.statistics.service;

import com.edutrack.domain.assignment.entity.Assignment;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.StudentExamStatus;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentLectureAverageResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;
import com.edutrack.domain.unit.entity.Unit;
import com.edutrack.domain.unit.repository.UnitRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentReportService {

    private final ExamStudentRepository examStudentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final UnitStatisticsRepository unitStatisticsRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;

    private static final int DEFAULT_WEAK_UNITS_COUNT = 3;

    /**
     * 학생 통합 분석 리포트 조회
     */
    public StudentAnalysisResponse getStudentAnalysis(Long studentId) {
        validateStudentExists(studentId);

        List<ExamStudent> examRecords = examStudentRepository.findAllByStudentIdWithExam(studentId);

        // 채점 완료된 시험만 필터링
        List<ExamStudent> gradedExams = filterGradedExams(examRecords);

        // 평균 점수
        Double avgScore = gradedExams.stream()
                .mapToInt(ExamStudent::getEarnedScore)
                .average()
                .orElse(0.0);

        // 취약 단원
        List<String> unitWeak = getWeakUnitNames(studentId, DEFAULT_WEAK_UNITS_COUNT);

        // 점수 추이 (시간순)
        List<Integer> trend = gradedExams.stream()
                .filter(es -> es.getEarnedScore() != null)
                .sorted(Comparator.comparing(ExamStudent::getSubmittedAt))
                .map(ExamStudent::getEarnedScore)
                .collect(Collectors.toList());

        return StudentAnalysisResponse.builder()
                .studentId(studentId)
                .avgScore(avgScore)
                .unitWeak(unitWeak)
                .trend(trend)
                .build();
    }

    /**
     * 학생의 전체 시험 요약 조회
     */
    public List<StudentExamSummaryResponse> getExamSummary(Long studentId) {
        validateStudentExists(studentId);

        List<ExamStudent> examRecords = examStudentRepository.findAllByStudentIdWithExam(studentId);
        List<ExamStudent> gradedExams = filterGradedExams(examRecords);

        return gradedExams.stream()
                .map(es -> StudentExamSummaryResponse.builder()
                        .examId(es.getExam().getId())
                        .examTitle(es.getExam().getTitle())
                        .lectureName(es.getExam().getLecture().getTitle())
                        .totalScore(Optional.ofNullable(es.getExam().getTotalScore()).orElse(0))
                        .earnedScore(Optional.ofNullable(es.getEarnedScore()).orElse(0))
                        .submittedAt(es.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> getWeakUnitNames(Long studentId, int limit) {
        List<StudentUnitCorrectRateResponse> weakUnits = unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId);

        return weakUnits.stream()
                .limit(limit)
                .map(u -> unitRepository.findById(u.getUnitId())
                        .map(Unit::getName)
                        .orElse("단원 정보 없음"))
                .collect(Collectors.toList());
    }

    /**
     * 학생의 단원별 성취도 조회 (정답률 낮은 순)
     */
    public List<StudentUnitCorrectRateResponse> getWeakUnits(Long studentId, int limit) {
        validateStudentExists(studentId);

        List<StudentUnitCorrectRateResponse> allWeakUnits = unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId);

        if (allWeakUnits.size() <= limit) {
            return allWeakUnits;
        }
        return allWeakUnits.subList(0, limit);
    }

    /**
     * 학생의 특정 강의에 대한 모든 시험 및 과제 결과 평균 점수 조회
     */
    public StudentLectureAverageResponse getLectureAverageScores(Long studentId, Long lectureId) {
        validateStudentExists(studentId);

        // 해당 강의의 모든 시험 응시 기록 조회
        List<ExamStudent> examStudents = examStudentRepository.findByStudentIdAndLectureId(studentId, lectureId);

        // 채점 완료된 시험만 필터링
        List<ExamStudent> gradedExams = filterGradedExams(examStudents);

        // 시험 평균 점수 계산 및 반올림
        Double examAverageScore = (double) Math.round(gradedExams.stream()
                .mapToInt(ExamStudent::getEarnedScore)
                .average()
                .orElse(0.0));

        // 해당 강의의 과제 목록 조회
        List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);
        List<Long> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .toList();

        // 학생의 과제 제출 기록 조회
        List<AssignmentSubmission> submissions = assignmentIds.isEmpty() 
                ? List.of() 
                : assignmentSubmissionRepository.findByAssignmentIdInAndStudentId(assignmentIds, studentId);

        // 채점된 과제만 필터링 (score가 null이 아닌 것)
        List<AssignmentSubmission> gradedAssignments = submissions.stream()
                .filter(s -> s.getScore() != null)
                .toList();

        // 과제 평균 점수 계산 및 반올림
        Double assignmentAverageScore = (double) Math.round(gradedAssignments.stream()
                .mapToInt(AssignmentSubmission::getScore)
                .average()
                .orElse(0.0));

        return StudentLectureAverageResponse.builder()
                .lectureId(lectureId)
                .examAverageGrade(examAverageScore)
                .assignmentAverageScore(assignmentAverageScore)
                .build();
    }

    /**
     * 채점 완료된 시험만 필터링
     */
    private List<ExamStudent> filterGradedExams(List<ExamStudent> examStudents) {
        return examStudents.stream()
                .filter(es -> es.getStatus() == StudentExamStatus.GRADED && es.getEarnedScore() != null)
                .toList();
    }

    private void validateStudentExists(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId);
        }
    }
}
