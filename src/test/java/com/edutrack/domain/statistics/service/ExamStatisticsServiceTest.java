package com.edutrack.domain.statistics.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.statistics.dto.ExamDistributionResponse;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExamStatisticsServiceTest {

    @InjectMocks
    private ExamStatisticsService examStatisticsService;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamStudentRepository examStudentRepository;

    @Nested
    @DisplayName("getScoreDistribution 메서드")
    class GetScoreDistribution {

        @Test
        @DisplayName("성공: 점수 분포를 올바르게 계산한다")
        void success_calculatesDistributionCorrectly() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudentWithScore(5),   // 0-10 구간
                    createExamStudentWithScore(15),  // 10-20 구간
                    createExamStudentWithScore(25),  // 20-30 구간
                    createExamStudentWithScore(55),  // 50-60 구간
                    createExamStudentWithScore(85),  // 80-90 구간
                    createExamStudentWithScore(95),  // 90-100 구간
                    createExamStudentWithScore(100)  // 90-100 구간
            );
            given(examStudentRepository.findByExam_Id(examId)).willReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getTotalSubmissions()).isEqualTo(7);
            assertThat(response.getRanges()).hasSize(10);
            assertThat(response.getCounts()).hasSize(10);

            // 각 구간별 카운트 검증
            assertThat(response.getCounts().get(0)).isEqualTo(1); // 0-10: 5점
            assertThat(response.getCounts().get(1)).isEqualTo(1); // 10-20: 15점
            assertThat(response.getCounts().get(2)).isEqualTo(1); // 20-30: 25점
            assertThat(response.getCounts().get(5)).isEqualTo(1); // 50-60: 55점
            assertThat(response.getCounts().get(8)).isEqualTo(1); // 80-90: 85점
            assertThat(response.getCounts().get(9)).isEqualTo(2); // 90-100: 95점, 100점
        }

        @Test
        @DisplayName("성공: 제출이 없을 경우 빈 분포를 반환한다")
        void success_returnsEmptyDistributionWhenNoSubmissions() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);
            given(examStudentRepository.findByExam_Id(examId)).willReturn(Collections.emptyList());

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getTotalSubmissions()).isEqualTo(0);
            assertThat(response.getRanges()).hasSize(10);
            assertThat(response.getCounts()).containsOnly(0);
        }

        @Test
        @DisplayName("성공: earnedScore가 null인 제출은 제외한다")
        void success_excludesNullScores() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudentWithScore(50),
                    createExamStudentWithScore(null), // null 점수
                    createExamStudentWithScore(70)
            );
            given(examStudentRepository.findByExam_Id(examId)).willReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getTotalSubmissions()).isEqualTo(2);
            assertThat(response.getCounts().get(5)).isEqualTo(1); // 50-60: 50점
            assertThat(response.getCounts().get(7)).isEqualTo(1); // 70-80: 70점
        }

        @Test
        @DisplayName("성공: 경계값 점수가 올바른 구간에 배치된다")
        void success_boundaryScoresPlacedCorrectly() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudentWithScore(0),   // 0-10 구간
                    createExamStudentWithScore(10),  // 10-20 구간
                    createExamStudentWithScore(90),  // 90-100 구간
                    createExamStudentWithScore(100)  // 90-100 구간 (Math.min으로 9로 제한)
            );
            given(examStudentRepository.findByExam_Id(examId)).willReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getCounts().get(0)).isEqualTo(1); // 0-10: 0점
            assertThat(response.getCounts().get(1)).isEqualTo(1); // 10-20: 10점
            assertThat(response.getCounts().get(9)).isEqualTo(2); // 90-100: 90점, 100점
        }

        @Test
        @DisplayName("성공: 정의된 범위 문자열이 올바르게 반환된다")
        void success_returnsCorrectRangeStrings() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);
            given(examStudentRepository.findByExam_Id(examId)).willReturn(Collections.emptyList());

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getRanges()).containsExactly(
                    "0-10", "10-20", "20-30", "30-40", "40-50",
                    "50-60", "60-70", "70-80", "80-90", "90-100"
            );
        }

        @Test
        @DisplayName("실패: 존재하지 않는 시험 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenExamNotExists() {
            // given
            Long examId = 999L;
            given(examRepository.existsById(examId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> examStatisticsService.getScoreDistribution(examId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("시험을 찾을수없습니다");
        }

        @Test
        @DisplayName("성공: 동일 구간에 여러 점수가 올바르게 집계된다")
        void success_aggregatesMultipleScoresInSameRange() {
            // given
            Long examId = 1L;
            given(examRepository.existsById(examId)).willReturn(true);

            List<ExamStudent> submissions = Arrays.asList(
                    createExamStudentWithScore(81),
                    createExamStudentWithScore(82),
                    createExamStudentWithScore(83),
                    createExamStudentWithScore(84),
                    createExamStudentWithScore(85)
            );
            given(examStudentRepository.findByExam_Id(examId)).willReturn(submissions);

            // when
            ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

            // then
            assertThat(response.getTotalSubmissions()).isEqualTo(5);
            assertThat(response.getCounts().get(8)).isEqualTo(5); // 80-90 구간에 5명
        }
    }

    private ExamStudent createExamStudentWithScore(Integer score) {
        ExamStudent examStudent = new ExamStudent();
        ReflectionTestUtils.setField(examStudent, "earnedScore", score);
        return examStudent;
    }
}
