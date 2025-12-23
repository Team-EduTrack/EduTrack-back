package com.edutrack.domain.statistics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edutrack.domain.exam.entity.Difficulty;
import com.edutrack.domain.statistics.dto.DifficultyStatisticsResponse;
import com.edutrack.domain.statistics.repository.ExamStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class ExamDifficultyStatisticsServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ExamDifficultyStatisticsServiceTest.class);

    @InjectMocks
    private ExamDifficultyStatisticsService examStatisticsService;

    @Mock
    private ExamStatisticsRepository examStatisticsRepository;

    private Long examId;
    private Long studentId;
    private Long lectureId;

    @BeforeEach
    void setUp() {
        examId = 1L;
        studentId = 100L;
        lectureId = 10L;
    }

    @Test
    @DisplayName("여러 학생이 같은 시험을 응시하여 난이도별 정답률 집계")
    void 여러_학생이_같은_시험을_응시하여_난이도별_정답률_집계() {
        // given
        log.info("=== 여러 학생의 답안 집계 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        log.info("시나리오: 학생 A, B, C가 같은 시험을 응시");
        log.info("  - 학생 A: EASY 문제 5개 중 4개 정답, MEDIUM 문제 5개 중 3개 정답, HARD 문제 2개 중 1개 정답");
        log.info("  - 학생 B: EASY 문제 5개 중 3개 정답, MEDIUM 문제 5개 중 4개 정답, HARD 문제 2개 중 0개 정답");
        log.info("  - 학생 C: EASY 문제 5개 중 5개 정답, MEDIUM 문제 5개 중 3개 정답, HARD 문제 2개 중 1개 정답");
        log.info("집계 결과: EASY 15개 중 12개 정답(0.8), MEDIUM 15개 중 10개 정답(0.67), HARD 6개 중 2개 정답(0.33)");
        
        // Repository 쿼리가 여러 학생의 답안을 집계한 결과
        List<DifficultyStatisticsResponse> results = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 15L, 12L),      // 학생 A+B+C의 EASY 문제 집계: 15개 중 12개 정답
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 15L, 10L),   // 학생 A+B+C의 MEDIUM 문제 집계: 15개 중 10개 정답
            new DifficultyStatisticsResponse(Difficulty.HARD, 6L, 2L)        // 학생 A+B+C의 HARD 문제 집계: 6개 중 2개 정답
        );
        log.info("Mock 데이터: EASY(15/12), MEDIUM(15/10), HARD(6/2)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId)).thenReturn(results);

        //when
        log.info("서비스 메서드 호출 - getDifficultyCorrectRateMapForExam");
        Map<Difficulty, Double> result = examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

         // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.8, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.8) - 여러 학생의 EASY 문제 집계", result.get(Difficulty.EASY));
        
        assertEquals(0.67, result.get(Difficulty.MEDIUM), 0.01);
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.67) - 여러 학생의 MEDIUM 문제 집계", result.get(Difficulty.MEDIUM));
    
        assertEquals(0.33, result.get(Difficulty.HARD), 0.01);
        log.info("HARD 난이도 정답률: {} (기대값: 0.33) - 여러 학생의 HARD 문제 집계", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 여러 학생의 답안 집계 테스트 완료 ===");
    }

    @Test
    @DisplayName("빈 결과일 때 모든 난이도가 0.0으로 반환")
    void 빈_결과일_때_모든_난이도가_0으로_반환() {
        // given
        log.info("=== 빈 결과 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(List.of());
        log.info("Mock 데이터: 빈 리스트 반환");

        // when
        log.info("서비스 메서드 호출");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.0, result.get(Difficulty.EASY));
        log.info("EASY 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.EASY));
        
        assertEquals(0.0, result.get(Difficulty.MEDIUM));
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.MEDIUM));
        
        assertEquals(0.0, result.get(Difficulty.HARD));
        log.info("HARD 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 빈 결과 테스트 완료 ===");
    }

    @Test
    @DisplayName("일부 난이도만 있는 경우 없는 난이도는 0.0으로 설정")
    void 일부_난이도만_있는_경우_없는_난이도는_0으로_설정() {
        // given
        log.info("=== 일부 난이도만 있는 경우 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        List<DifficultyStatisticsResponse> results = new ArrayList<>();
        results.add(new DifficultyStatisticsResponse(Difficulty.EASY, 10L, 8L));
        // MEDIUM, HARD는 없음
        log.info("Mock 데이터: EASY만 존재 (10문제 중 8문제 정답)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.8, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.8)", result.get(Difficulty.EASY));
        
        assertEquals(0.0, result.get(Difficulty.MEDIUM));
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.0 - 없음)", result.get(Difficulty.MEDIUM));
        
        assertEquals(0.0, result.get(Difficulty.HARD));
        log.info("HARD 난이도 정답률: {} (기대값: 0.0 - 없음)", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 일부 난이도만 있는 경우 테스트 완료 ===");
    }

    @Test
    @DisplayName("정답률이 100%인 경우")
    void 정답률이_100퍼센트인_경우() {
        // given
        log.info("=== 정답률 100% 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        List<DifficultyStatisticsResponse> results = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 10L, 10L),    // 10문제 모두 정답
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 5L, 5L),     // 5문제 모두 정답
            new DifficultyStatisticsResponse(Difficulty.HARD, 3L, 3L)       // 3문제 모두 정답
        );
        log.info("Mock 데이터: 모든 난이도 100% 정답률");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(1.0, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 1.0)", result.get(Difficulty.EASY));
        
        assertEquals(1.0, result.get(Difficulty.MEDIUM), 0.01);
        log.info("MEDIUM 난이도 정답률: {} (기대값: 1.0)", result.get(Difficulty.MEDIUM));
        
        assertEquals(1.0, result.get(Difficulty.HARD), 0.01);
        log.info("HARD 난이도 정답률: {} (기대값: 1.0)", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 정답률 100% 테스트 완료 ===");
    }

    @Test
    @DisplayName("정답률이 0%인 경우")
    void 정답률이_0퍼센트인_경우() {
        // given
        log.info("=== 정답률 0% 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        List<DifficultyStatisticsResponse> results = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 10L, 0L),      // 10문제 모두 오답
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 5L, 0L),    // 5문제 모두 오답
            new DifficultyStatisticsResponse(Difficulty.HARD, 3L, 0L)       // 3문제 모두 오답
        );
        log.info("Mock 데이터: 모든 난이도 0% 정답률");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.0, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.EASY));
        
        assertEquals(0.0, result.get(Difficulty.MEDIUM), 0.01);
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.MEDIUM));
        
        assertEquals(0.0, result.get(Difficulty.HARD), 0.01);
        log.info("HARD 난이도 정답률: {} (기대값: 0.0)", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 정답률 0% 테스트 완료 ===");
    }

    @Test
    @DisplayName("전체 문제 수가 0인 경우 0으로 나누기 방지")
    void 전체_문제_수가_0인_경우_0으로_나누기_방지() {
        // given
        log.info("=== 0으로 나누기 방지 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        List<DifficultyStatisticsResponse> results = new ArrayList<>();
        results.add(new DifficultyStatisticsResponse(Difficulty.EASY, 0L, 0L));    // 전체 문제 수가 0
        log.info("Mock 데이터: totalQuestions = 0 (0으로 나누기 방지 확인)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForExam(examId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.0, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.0 - 0으로 나누기 방지)", result.get(Difficulty.EASY));
        
        assertEquals(0.0, result.get(Difficulty.MEDIUM));
        assertEquals(0.0, result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 0으로 나누기 방지 테스트 완료 ===");
    }

    @Test
    @DisplayName("한 학생이 시험을 응시하여 난이도별 정답률 집계")
    void 한_학생이_시험을_응시하여_난이도별_정답률_집계() {
        // given
        log.info("=== 한 학생의 답안 집계 테스트 시작 ===");
        log.info("시험 ID: {}, 학생 ID: {}", examId, studentId);
        log.info("시나리오: 학생이 시험을 응시하여 각 난이도별 문제를 풀었음");
        log.info("  - EASY 난이도 문제 5개 중 4개 정답 (정답률 0.8)");
        log.info("  - MEDIUM 난이도 문제 8개 중 6개 정답 (정답률 0.75)");
        log.info("  - HARD 난이도 문제 2개 중 1개 정답 (정답률 0.5)");
        log.info("참고: 난이도는 문제마다 설정되어 있으며, 같은 난이도의 문제들을 그룹화하여 집계");
        
        // 한 학생의 답안을 난이도별로 집계한 결과
        List<DifficultyStatisticsResponse> results = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 5L, 4L),      // 학생이 푼 EASY 문제 5개 중 4개 정답
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 8L, 6L),   // 학생이 푼 MEDIUM 문제 8개 중 6개 정답
            new DifficultyStatisticsResponse(Difficulty.HARD, 2L, 1L)      // 학생이 푼 HARD 문제 2개 중 1개 정답
        );
        log.info("Mock 데이터: EASY(5/4), MEDIUM(8/6), HARD(2/1)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamIdAndStudentId(examId, studentId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출 - getDifficultyCorrectRateMapForStudentExam");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForStudentExam(examId, studentId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.8, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.8) - 학생이 푼 EASY 문제들의 정답률", result.get(Difficulty.EASY));
        
        assertEquals(0.75, result.get(Difficulty.MEDIUM), 0.01);
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.75) - 학생이 푼 MEDIUM 문제들의 정답률", result.get(Difficulty.MEDIUM));
        
        assertEquals(0.5, result.get(Difficulty.HARD), 0.01);
        log.info("HARD 난이도 정답률: {} (기대값: 0.5) - 학생이 푼 HARD 문제들의 정답률", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamIdAndStudentId(examId, studentId);
        log.info("=== 한 학생의 답안 집계 테스트 완료 ===");
    }

    @Test
    @DisplayName("강의 전체의 난이도별 정답률 통계 계산 성공")
    void 강의_전체의_난이도별_정답률_통계_계산_성공() {
        // given
        log.info("=== 강의 전체 통계 테스트 시작 ===");
        log.info("강의 ID: {}", lectureId);
        
        List<DifficultyStatisticsResponse> results = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 50L, 40L),
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 30L, 20L),
            new DifficultyStatisticsResponse(Difficulty.HARD, 20L, 8L)
        );
        log.info("Mock 데이터: EASY(50/40), MEDIUM(30/20), HARD(20/8)");

        when(examStatisticsRepository.findDifficultyStatisticsByLectureId(lectureId))
            .thenReturn(results);

        // when
        log.info("서비스 메서드 호출 - getDifficultyCorrectRateMapForLecture");
        Map<Difficulty, Double> result = 
            examStatisticsService.getDifficultyCorrectRateMapForLecture(lectureId);

        // then
        log.info("=== 계산 결과 검증 ===");
        assertEquals(0.8, result.get(Difficulty.EASY), 0.01);
        log.info("EASY 난이도 정답률: {} (기대값: 0.8)", result.get(Difficulty.EASY));
        
        assertEquals(0.67, result.get(Difficulty.MEDIUM), 0.01);
        log.info("MEDIUM 난이도 정답률: {} (기대값: 0.67)", result.get(Difficulty.MEDIUM));
        
        assertEquals(0.4, result.get(Difficulty.HARD), 0.01);
        log.info("HARD 난이도 정답률: {} (기대값: 0.4)", result.get(Difficulty.HARD));
        
        verify(examStatisticsRepository).findDifficultyStatisticsByLectureId(lectureId);
        log.info("=== 강의 전체 통계 테스트 완료 ===");
    }

    // ------------------------------------------------------------
    // List<DifficultyStatisticsResponse> 반환 메서드 테스트
    // ------------------------------------------------------------

    @Test
    @DisplayName("특정 시험의 난이도별 통계 조회 - 모든 난이도 포함")
    void 특정_시험의_난이도별_통계_조회_모든_난이도_포함() {
        // given
        log.info("=== 특정 시험 난이도별 통계 조회 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        List<DifficultyStatisticsResponse> repositoryResults = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 10L, 8L),
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 10L, 6L),
            new DifficultyStatisticsResponse(Difficulty.HARD, 5L, 2L)
        );
        log.info("Mock 데이터: EASY(10/8), MEDIUM(10/6), HARD(5/2)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(repositoryResults);

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForExam");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForExam(examId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도(EASY, MEDIUM, HARD)가 포함되어야 함");
        
        // EASY 검증
        DifficultyStatisticsResponse easyStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.EASY)
            .findFirst()
            .orElse(null);
        assertNotNull(easyStat);
        assertEquals(10L, easyStat.getTotalQuestions());
        assertEquals(8L, easyStat.getCorrectQuestions());
        log.info("EASY 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            easyStat.getTotalQuestions(), easyStat.getCorrectQuestions());
        
        // MEDIUM 검증
        DifficultyStatisticsResponse mediumStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.MEDIUM)
            .findFirst()
            .orElse(null);
        assertNotNull(mediumStat);
        assertEquals(10L, mediumStat.getTotalQuestions());
        assertEquals(6L, mediumStat.getCorrectQuestions());
        log.info("MEDIUM 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            mediumStat.getTotalQuestions(), mediumStat.getCorrectQuestions());
        
        // HARD 검증
        DifficultyStatisticsResponse hardStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.HARD)
            .findFirst()
            .orElse(null);
        assertNotNull(hardStat);
        assertEquals(5L, hardStat.getTotalQuestions());
        assertEquals(2L, hardStat.getCorrectQuestions());
        log.info("HARD 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            hardStat.getTotalQuestions(), hardStat.getCorrectQuestions());
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 특정 시험 난이도별 통계 조회 테스트 완료 ===");
    }

    @Test
    @DisplayName("특정 시험의 난이도별 통계 조회 - 일부 난이도만 있는 경우 기본값 포함")
    void 특정_시험의_난이도별_통계_조회_일부_난이도만_있는_경우() {
        // given
        log.info("=== 일부 난이도만 있는 경우 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        // EASY만 있는 경우
        List<DifficultyStatisticsResponse> repositoryResults = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 10L, 8L)
        );
        log.info("Mock 데이터: EASY만 존재 (10/8)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(repositoryResults);

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForExam");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForExam(examId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        // EASY는 실제 데이터
        DifficultyStatisticsResponse easyStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.EASY)
            .findFirst()
            .orElse(null);
        assertNotNull(easyStat);
        assertEquals(10L, easyStat.getTotalQuestions());
        assertEquals(8L, easyStat.getCorrectQuestions());
        log.info("EASY 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            easyStat.getTotalQuestions(), easyStat.getCorrectQuestions());
        
        // MEDIUM과 HARD는 기본값(0, 0)
        DifficultyStatisticsResponse mediumStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.MEDIUM)
            .findFirst()
            .orElse(null);
        assertNotNull(mediumStat);
        assertEquals(0L, mediumStat.getTotalQuestions());
        assertEquals(0L, mediumStat.getCorrectQuestions());
        log.info("MEDIUM 난이도 통계: 기본값 (0/0)");
        
        DifficultyStatisticsResponse hardStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.HARD)
            .findFirst()
            .orElse(null);
        assertNotNull(hardStat);
        assertEquals(0L, hardStat.getTotalQuestions());
        assertEquals(0L, hardStat.getCorrectQuestions());
        log.info("HARD 난이도 통계: 기본값 (0/0)");
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 일부 난이도만 있는 경우 테스트 완료 ===");
    }

    @Test
    @DisplayName("특정 시험의 난이도별 통계 조회 - 빈 결과일 때 모든 난이도 기본값 반환")
    void 특정_시험의_난이도별_통계_조회_빈_결과일_때() {
        // given
        log.info("=== 빈 결과 테스트 시작 ===");
        log.info("시험 ID: {}", examId);
        
        when(examStatisticsRepository.findDifficultyStatisticsByExamId(examId))
            .thenReturn(List.of());
        log.info("Mock 데이터: 빈 리스트 반환");

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForExam");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForExam(examId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        // 모든 난이도가 기본값(0, 0)인지 확인
        for (DifficultyStatisticsResponse stat : result) {
            assertEquals(0L, stat.getTotalQuestions());
            assertEquals(0L, stat.getCorrectQuestions());
            log.info("{} 난이도 통계: 기본값 (0/0)", stat.getDifficulty());
        }
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamId(examId);
        log.info("=== 빈 결과 테스트 완료 ===");
    }

    @Test
    @DisplayName("특정 학생의 특정 시험 난이도별 통계 조회 성공")
    void 특정_학생의_특정_시험_난이도별_통계_조회_성공() {
        // given
        log.info("=== 특정 학생의 시험 난이도별 통계 조회 테스트 시작 ===");
        log.info("시험 ID: {}, 학생 ID: {}", examId, studentId);
        
        List<DifficultyStatisticsResponse> repositoryResults = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 5L, 4L),
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 8L, 6L),
            new DifficultyStatisticsResponse(Difficulty.HARD, 2L, 1L)
        );
        log.info("Mock 데이터: EASY(5/4), MEDIUM(8/6), HARD(2/1)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamIdAndStudentId(examId, studentId))
            .thenReturn(repositoryResults);

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForStudentExam");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForStudentExam(examId, studentId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        DifficultyStatisticsResponse easyStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.EASY)
            .findFirst()
            .orElse(null);
        assertNotNull(easyStat);
        assertEquals(5L, easyStat.getTotalQuestions());
        assertEquals(4L, easyStat.getCorrectQuestions());
        log.info("EASY 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            easyStat.getTotalQuestions(), easyStat.getCorrectQuestions());
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamIdAndStudentId(examId, studentId);
        log.info("=== 특정 학생의 시험 난이도별 통계 조회 테스트 완료 ===");
    }

    @Test
    @DisplayName("특정 학생의 특정 시험 난이도별 통계 조회 - 일부 난이도만 있는 경우")
    void 특정_학생의_특정_시험_난이도별_통계_조회_일부_난이도만_있는_경우() {
        // given
        log.info("=== 특정 학생의 시험 - 일부 난이도만 있는 경우 테스트 시작 ===");
        log.info("시험 ID: {}, 학생 ID: {}", examId, studentId);
        
        // EASY와 MEDIUM만 있는 경우
        List<DifficultyStatisticsResponse> repositoryResults = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 5L, 4L),
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 8L, 6L)
        );
        log.info("Mock 데이터: EASY(5/4), MEDIUM(8/6)");

        when(examStatisticsRepository.findDifficultyStatisticsByExamIdAndStudentId(examId, studentId))
            .thenReturn(repositoryResults);

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForStudentExam");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForStudentExam(examId, studentId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        // HARD는 기본값이어야 함
        DifficultyStatisticsResponse hardStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.HARD)
            .findFirst()
            .orElse(null);
        assertNotNull(hardStat);
        assertEquals(0L, hardStat.getTotalQuestions());
        assertEquals(0L, hardStat.getCorrectQuestions());
        log.info("HARD 난이도 통계: 기본값 (0/0)");
        
        verify(examStatisticsRepository).findDifficultyStatisticsByExamIdAndStudentId(examId, studentId);
        log.info("=== 특정 학생의 시험 - 일부 난이도만 있는 경우 테스트 완료 ===");
    }

    @Test
    @DisplayName("강의의 모든 시험 난이도별 통계 조회 성공")
    void 강의의_모든_시험_난이도별_통계_조회_성공() {
        // given
        log.info("=== 강의의 모든 시험 난이도별 통계 조회 테스트 시작 ===");
        log.info("강의 ID: {}", lectureId);
        
        List<DifficultyStatisticsResponse> repositoryResults = List.of(
            new DifficultyStatisticsResponse(Difficulty.EASY, 50L, 40L),
            new DifficultyStatisticsResponse(Difficulty.MEDIUM, 30L, 20L),
            new DifficultyStatisticsResponse(Difficulty.HARD, 20L, 8L)
        );
        log.info("Mock 데이터: EASY(50/40), MEDIUM(30/20), HARD(20/8)");

        when(examStatisticsRepository.findDifficultyStatisticsByLectureId(lectureId))
            .thenReturn(repositoryResults);

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForLecture");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForLecture(lectureId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        DifficultyStatisticsResponse easyStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.EASY)
            .findFirst()
            .orElse(null);
        assertNotNull(easyStat);
        assertEquals(50L, easyStat.getTotalQuestions());
        assertEquals(40L, easyStat.getCorrectQuestions());
        log.info("EASY 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            easyStat.getTotalQuestions(), easyStat.getCorrectQuestions());
        
        DifficultyStatisticsResponse mediumStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.MEDIUM)
            .findFirst()
            .orElse(null);
        assertNotNull(mediumStat);
        assertEquals(30L, mediumStat.getTotalQuestions());
        assertEquals(20L, mediumStat.getCorrectQuestions());
        log.info("MEDIUM 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            mediumStat.getTotalQuestions(), mediumStat.getCorrectQuestions());
        
        DifficultyStatisticsResponse hardStat = result.stream()
            .filter(r -> r.getDifficulty() == Difficulty.HARD)
            .findFirst()
            .orElse(null);
        assertNotNull(hardStat);
        assertEquals(20L, hardStat.getTotalQuestions());
        assertEquals(8L, hardStat.getCorrectQuestions());
        log.info("HARD 난이도 통계: 전체 {}문제 중 {}문제 정답", 
            hardStat.getTotalQuestions(), hardStat.getCorrectQuestions());
        
        verify(examStatisticsRepository).findDifficultyStatisticsByLectureId(lectureId);
        log.info("=== 강의의 모든 시험 난이도별 통계 조회 테스트 완료 ===");
    }

    @Test
    @DisplayName("강의의 모든 시험 난이도별 통계 조회 - 빈 결과일 때")
    void 강의의_모든_시험_난이도별_통계_조회_빈_결과일_때() {
        // given
        log.info("=== 강의의 모든 시험 - 빈 결과 테스트 시작 ===");
        log.info("강의 ID: {}", lectureId);
        
        when(examStatisticsRepository.findDifficultyStatisticsByLectureId(lectureId))
            .thenReturn(List.of());
        log.info("Mock 데이터: 빈 리스트 반환");

        // when
        log.info("서비스 메서드 호출 - getDifficultyStatisticsForLecture");
        List<DifficultyStatisticsResponse> result = 
            examStatisticsService.getDifficultyStatisticsForLecture(lectureId);

        // then
        log.info("=== 결과 검증 ===");
        assertNotNull(result);
        assertEquals(3, result.size(), "모든 난이도가 포함되어야 함");
        
        // 모든 난이도가 기본값(0, 0)인지 확인
        for (DifficultyStatisticsResponse stat : result) {
            assertEquals(0L, stat.getTotalQuestions());
            assertEquals(0L, stat.getCorrectQuestions());
            log.info("{} 난이도 통계: 기본값 (0/0)", stat.getDifficulty());
        }
        
        verify(examStatisticsRepository).findDifficultyStatisticsByLectureId(lectureId);
        log.info("=== 강의의 모든 시험 - 빈 결과 테스트 완료 ===");
    }

}
