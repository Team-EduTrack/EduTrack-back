package com.edutrack.domain.statistics.service;

import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitStatisticsService {

    private final UnitStatisticsRepository unitStatisticsRepository;

    @Transactional(readOnly = true)
    public UnitCorrectRateResponse getUnitCorrectRate(Long unitId) {
        return unitStatisticsRepository.findUnitCorrectRate(unitId)
                .orElseGet(() -> new UnitCorrectRateResponse(unitId, 0, 0, 0.0));
    }

    @Transactional(readOnly = true)
    public StudentUnitCorrectRateResponse getStudentUnitCorrectRate(Long studentId, Long unitId) {
        return unitStatisticsRepository.findStudentUnitCorrectRate(studentId, unitId)
                .orElseGet(() -> new StudentUnitCorrectRateResponse(unitId, studentId, 0, 0, 0.0));
    }

    /**
     * 특정 강의의 전체 수강생 단원별 정답률 조회
     */
    @Transactional(readOnly = true)
    public List<UnitCorrectRateResponse> getAllUnitCorrectRatesByLectureId(Long lectureId) {
        return unitStatisticsRepository.findAllUnitCorrectRatesByLectureId(lectureId);
    }
}