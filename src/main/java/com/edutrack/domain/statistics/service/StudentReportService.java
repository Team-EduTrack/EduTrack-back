package com.edutrack.domain.statistics.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.StudentExamStatus;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;
import com.edutrack.domain.unit.entity.Unit;
import com.edutrack.domain.unit.repository.UnitRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentReportService {

    private final ExamStudentRepository examStudentRepository;
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
        List<ExamStudent> gradedExams = examRecords.stream()
                .filter(es -> es.getStatus() == StudentExamStatus.GRADED && es.getEarnedScore() != null)
                .toList();

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

        return examStudentRepository.findAllByStudentIdWithExam(studentId).stream()
                .filter(es -> es.getStatus() == StudentExamStatus.GRADED && es.getEarnedScore() != null)
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

    private void validateStudentExists(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId);
        }
    }
}
