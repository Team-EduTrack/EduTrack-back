package com.edutrack.domain.statistics.service;

import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}