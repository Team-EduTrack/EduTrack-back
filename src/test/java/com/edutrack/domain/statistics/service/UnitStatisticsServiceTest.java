package com.edutrack.domain.statistics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class UnitStatisticsServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UnitStatisticsServiceTest.class);

    @InjectMocks
    private UnitStatisticsService unitStatisticsService;

    @Mock
    private UnitStatisticsRepository unitStatisticsRepository;

    private Long unitId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        unitId = 10L;
        studentId = 100L;
    }

    // ------------------------------------------------------------
    // 1. 전체 학생 단원 정답률 테스트
    // ------------------------------------------------------------
    @Test
    @DisplayName("전체 학생 기준 단원 정답률을 올바르게 계산한다")
    void 전체_학생_기준_단원별_정답률_계산() {
        log.info("=== 전체 학생 기준 단원 정답률 테스트 시작 ===");

        UnitCorrectRateResponse mockResponse =
                new UnitCorrectRateResponse(unitId, 20, 15, 75.0);

        when(unitStatisticsRepository.findUnitCorrectRate(unitId))
                .thenReturn(Optional.of(mockResponse));

        UnitCorrectRateResponse result = unitStatisticsService.getUnitCorrectRate(unitId);

        assertEquals(unitId, result.getUnitId());
        assertEquals(20, result.getTotalTryCount());
        assertEquals(15, result.getCorrectCount());
        assertEquals(75.0, result.getCorrectRate(), 0.01);

        verify(unitStatisticsRepository).findUnitCorrectRate(unitId);

        log.info("=== 전체 학생 기준 단원 정답률 테스트 완료 ===");
    }

    // ------------------------------------------------------------
    // 2. 특정 학생 단원 정답률 테스트
    // ------------------------------------------------------------
    @Test
    @DisplayName("특정 학생 기준 단원별 정답률을 올바르게 계산한다")
    void 특정_학생_기준_단원별_정답률_계산() {
        log.info("=== 특정 학생 기준 단원 정답률 테스트 시작 ===");

        StudentUnitCorrectRateResponse mockResponse =
                new StudentUnitCorrectRateResponse(unitId, studentId, 10, 7, 70.0);

        when(unitStatisticsRepository.findStudentUnitCorrectRate(studentId, unitId))
                .thenReturn(Optional.of(mockResponse));

        StudentUnitCorrectRateResponse result =
                unitStatisticsService.getStudentUnitCorrectRate(studentId, unitId);

        assertEquals(unitId, result.getUnitId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(10, result.getTotalTryCount());
        assertEquals(7, result.getCorrectCount());
        assertEquals(70.0, result.getCorrectRate(), 0.01);

        verify(unitStatisticsRepository)
                .findStudentUnitCorrectRate(studentId, unitId);

        log.info("=== 특정 학생 기준 단원 정답률 테스트 완료 ===");
    }

    // ------------------------------------------------------------
    // 3. 빈 결과일 때 기본값 반환 테스트
    // ------------------------------------------------------------
    @Test
    @DisplayName("빈 결과일 때 기본값(0)으로 처리한다")
    void 빈_결과일_때_기본값_처리() {
        log.info("=== 빈 결과 기본값 처리 테스트 시작 ===");

        when(unitStatisticsRepository.findUnitCorrectRate(unitId))
                .thenReturn(Optional.empty());

        UnitCorrectRateResponse result = unitStatisticsService.getUnitCorrectRate(unitId);

        assertEquals(unitId, result.getUnitId());
        assertEquals(0, result.getTotalTryCount());
        assertEquals(0, result.getCorrectCount());
        assertEquals(0.0, result.getCorrectRate(), 0.01);

        verify(unitStatisticsRepository).findUnitCorrectRate(unitId);

        log.info("=== 빈 결과 기본값 처리 테스트 완료 ===");
    }

    // ------------------------------------------------------------
    // 4. 전체 문제 수가 0인 경우 테스트
    // ------------------------------------------------------------
    @Test
    @DisplayName("전체 문제 수가 0인 경우 정답률 0으로 계산한다")
    void 전체_문제_수_0_정답률_처리() {
        log.info("=== 0 문제 테스트 시작 ===");

        UnitCorrectRateResponse mockResponse =
                new UnitCorrectRateResponse(unitId, 0, 0, 0.0);

        when(unitStatisticsRepository.findUnitCorrectRate(unitId))
                .thenReturn(Optional.of(mockResponse));

        UnitCorrectRateResponse result = unitStatisticsService.getUnitCorrectRate(unitId);

        assertEquals(0, result.getTotalTryCount());
        assertEquals(0, result.getCorrectCount());
        assertEquals(0.0, result.getCorrectRate(), 0.01);

        verify(unitStatisticsRepository).findUnitCorrectRate(unitId);

        log.info("=== 0 문제 테스트 완료 ===");
    }
}