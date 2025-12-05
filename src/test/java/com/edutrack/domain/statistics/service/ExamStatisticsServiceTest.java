package com.edutrack.domain.statistics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.statistics.dto.ExamDistributionResponse;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 시험 통계 서비스 테스트
 * - 점수 분포 히스토그램 데이터 생성 로직
 */
@ExtendWith(MockitoExtension.class)
class ExamStatisticsServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ExamStatisticsServiceTest.class);

    @InjectMocks
    private ExamStatisticsService examStatisticsService;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamStudentRepository examStudentRepository;

    private Long examId;

    @BeforeEach
    void setUp() {
        examId = 100L;
    }

    @Nested
    @DisplayName("점수 분포 조회 성공 테스트")
    class GetScoreDistributionSuccessTest {

        @Test
        @DisplayName("다양한 점수대의 학생들이 있을 때 올바른 분포를 반환해야 한다")
        void 점수_분포_조회_성공_다양한_점수() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam mockExam = mock(Exam.class);
            when(mockExam.getId()).thenReturn(examId);

            // 각 점수대별 학생 데이터 생성 (0-10, 10-20, ..., 90-100)
            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudent(mockExam, 5),   // 0-10
                    createExamStudent(mockExam, 15),  // 10-20
                    createExamStudent(mockExam, 25),  // 20-30
                    createExamStudent(mockExam, 35),  // 30-40
                    createExamStudent(mockExam, 45),  // 40-50
                    createExamStudent(mockExam, 55),  // 50-60
                    createExamStudent(mockExam, 65),  // 60-70
                    createExamStudent(mockExam, 75),  // 70-80
                    createExamStudent(mockExam, 85),  // 80-90
                    createExamStudent(mockExam, 95)   // 90-100
            );
            when(examStudentRepository.findAll()).thenReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertNotNull(response);
            assertEquals(10, response.getRanges().size());
            assertEquals(10, response.getCounts().size());
            assertEquals(10, response.getTotalSubmissions());

            // 각 구간에 1명씩 있어야 함
            for (int i = 0; i < 10; i++) {
                assertEquals(1, response.getCounts().get(i));
            }

            log.info("=== 다양한 점수대 분포 테스트 결과 ===");
            log.info("총 응시자 수: {}", response.getTotalSubmissions());
            log.info("점수 구간: {}", response.getRanges());
            log.info("구간별 인원: {}", response.getCounts());
        }

        @Test
        @DisplayName("특정 점수대에 학생들이 집중되어 있을 때 올바른 분포를 반환해야 한다")
        void 점수_분포_조회_성공_집중된_점수() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam mockExam = mock(Exam.class);
            when(mockExam.getId()).thenReturn(examId);

            // 70-80점대에 집중된 학생들
            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudent(mockExam, 72),
                    createExamStudent(mockExam, 75),
                    createExamStudent(mockExam, 78),
                    createExamStudent(mockExam, 71),
                    createExamStudent(mockExam, 85)   // 80-90
            );
            when(examStudentRepository.findAll()).thenReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertNotNull(response);
            assertEquals(4, response.getCounts().get(7)); // 70-80 구간에 4명
            assertEquals(1, response.getCounts().get(8)); // 80-90 구간에 1명
            assertEquals(5, response.getTotalSubmissions());

            log.info("=== 집중된 점수대 분포 테스트 결과 ===");
            log.info("70-80점대 학생 수: {}", response.getCounts().get(7));
        }

        @Test
        @DisplayName("만점(100점) 학생이 있을 때 90-100 구간에 포함되어야 한다")
        void 점수_분포_조회_성공_만점_학생() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam mockExam = mock(Exam.class);
            when(mockExam.getId()).thenReturn(examId);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudent(mockExam, 100),
                    createExamStudent(mockExam, 95)
            );
            when(examStudentRepository.findAll()).thenReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertEquals(2, response.getCounts().get(9)); // 90-100 구간에 2명

            log.info("=== 만점 학생 포함 테스트 결과 ===");
            log.info("90-100점대 학생 수: {}", response.getCounts().get(9));
        }

        @Test
        @DisplayName("0점 학생이 있을 때 0-10 구간에 포함되어야 한다")
        void 점수_분포_조회_성공_영점_학생() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam mockExam = mock(Exam.class);
            when(mockExam.getId()).thenReturn(examId);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudent(mockExam, 0),
                    createExamStudent(mockExam, 5)
            );
            when(examStudentRepository.findAll()).thenReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertEquals(2, response.getCounts().get(0)); // 0-10 구간에 2명

            log.info("=== 0점 학생 포함 테스트 결과 ===");
            log.info("0-10점대 학생 수: {}", response.getCounts().get(0));
        }

        @Test
        @DisplayName("응시 학생이 없을 때 빈 분포를 반환해야 한다")
        void 점수_분포_조회_성공_응시자_없음() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);
            when(examStudentRepository.findAll()).thenReturn(Collections.emptyList());

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertNotNull(response);
            assertEquals(10, response.getRanges().size());
            assertEquals(0, response.getTotalSubmissions());

            // 모든 구간이 0이어야 함
            for (Integer count : response.getCounts()) {
                assertEquals(0, count);
            }

            log.info("=== 응시자 없음 테스트 결과 ===");
            log.info("총 응시자 수: {}", response.getTotalSubmissions());
        }

        @Test
        @DisplayName("점수가 null인 학생은 집계에서 제외되어야 한다")
        void 점수_분포_조회_성공_null_점수_제외() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam mockExam = mock(Exam.class);
            when(mockExam.getId()).thenReturn(examId);

            ExamStudent studentWithScore = createExamStudent(mockExam, 85);
            ExamStudent studentWithNullScore = createExamStudentWithNullScore(mockExam);

            List<ExamStudent> submissions = Arrays.asList(studentWithScore, studentWithNullScore);
            when(examStudentRepository.findAll()).thenReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertEquals(1, response.getCounts().get(8)); // 80-90 구간에 1명만

            log.info("=== null 점수 제외 테스트 결과 ===");
            log.info("총 응시자 수 (데이터 기준): {}", response.getTotalSubmissions());
            log.info("실제 집계된 점수: 80-90 구간 = {}", response.getCounts().get(8));
        }

        @Test
        @DisplayName("다른 시험의 학생은 집계에서 제외되어야 한다")
        void 점수_분포_조회_성공_다른_시험_제외() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);

            Exam targetExam = mock(Exam.class);
            when(targetExam.getId()).thenReturn(examId);

            Exam otherExam = mock(Exam.class);
            when(otherExam.getId()).thenReturn(999L);

            ExamStudent targetExamStudent = createExamStudent(targetExam, 80);
            ExamStudent otherExamStudent = createExamStudent(otherExam, 90);

            List<ExamStudent> allSubmissions = Arrays.asList(targetExamStudent, otherExamStudent);
            when(examStudentRepository.findAll()).thenReturn(allSubmissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertEquals(1, response.getCounts().get(8)); // 80-90 구간에 1명
            assertEquals(0, response.getCounts().get(9)); // 90-100 구간에 0명 (다른 시험 학생 제외)

            log.info("=== 다른 시험 제외 테스트 결과 ===");
            log.info("대상 시험(ID={})의 80-90점대 학생 수: {}", examId, response.getCounts().get(8));
        }
    }

    @Nested
    @DisplayName("점수 분포 조회 실패 테스트")
    class GetScoreDistributionFailTest {

        @Test
        @DisplayName("존재하지 않는 시험 ID로 조회 시 NotFoundException을 던져야 한다")
        void 점수_분포_조회_실패_시험_없음() {
            // given
            Long nonExistentExamId = 999L;
            when(examRepository.existsById(nonExistentExamId)).thenReturn(false);

            // when & then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                examStatisticsService.getScoreDistribution(nonExistentExamId);
            });

            assertTrue(exception.getMessage().contains("시험을 찾을수없습니다"));

            log.info("=== 존재하지 않는 시험 조회 테스트 결과 ===");
            log.info("예외 메시지: {}", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("점수 구간 검증 테스트")
    class ScoreRangeValidationTest {

        @Test
        @DisplayName("점수 구간이 올바르게 정의되어 있어야 한다")
        void 점수_구간_정의_검증() {
            // given
            when(examRepository.existsById(examId)).thenReturn(true);
            when(examStudentRepository.findAll()).thenReturn(Collections.emptyList());

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            List<String> expectedRanges = Arrays.asList(
                    "0-10", "10-20", "20-30", "30-40", "40-50",
                    "50-60", "60-70", "70-80", "80-90", "90-100"
            );
            assertEquals(expectedRanges, response.getRanges());

            log.info("=== 점수 구간 정의 검증 결과 ===");
            log.info("정의된 점수 구간: {}", response.getRanges());
        }
    }

    // === Helper Methods ===

    private ExamStudent createExamStudent(Exam exam, int score) {
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.getExam()).thenReturn(exam);
        when(examStudent.getEarnedScore()).thenReturn(score);
        return examStudent;
    }

    private ExamStudent createExamStudentWithNullScore(Exam exam) {
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.getExam()).thenReturn(exam);
        when(examStudent.getEarnedScore()).thenReturn(null);
        return examStudent;
    }
}

