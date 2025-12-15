package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.ExamDistributionResponse;
import com.edutrack.domain.statistics.service.ExamStatisticsService;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExamStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExamStatisticsService examStatisticsService;

    private static final Long EXAM_ID = 1L;
    private static final List<String> DEFAULT_RANGES = Arrays.asList(
            "0-10", "10-20", "20-30", "30-40", "40-50",
            "50-60", "60-70", "70-80", "80-90", "90-100"
    );

    private ExamDistributionResponse createMockResponse(List<Integer> counts, int totalSubmissions) {
        return ExamDistributionResponse.builder()
                .ranges(DEFAULT_RANGES)
                .counts(counts)
                .totalSubmissions(totalSubmissions)
                .build();
    }

    @Nested
    @DisplayName("권한 검증 테스트")
    class AuthorizationTest {

        @Test
        @DisplayName("인증되지 않은 사용자 접근 시 403 Forbidden 반환해야 한다")
        void getDistribution_fail_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("STUDENT 역할로 접근 시 403 Forbidden 반환해야 한다")
        void getDistribution_fail_forbidden_student() throws Exception {
            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 역할로 접근 시 200 OK 반환해야 한다")
        void getDistribution_success_admin() throws Exception {
            // given
            ExamDistributionResponse response = createMockResponse(
                    Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), 0
            );
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "PRINCIPAL")
        @DisplayName("PRINCIPAL 역할로 접근 시 200 OK 반환해야 한다")
        void getDistribution_success_principal() throws Exception {
            // given
            ExamDistributionResponse response = createMockResponse(
                    Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), 0
            );
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("TEACHER 역할로 접근 시 200 OK 반환해야 한다")
        void getDistribution_success_teacher() throws Exception {
            // given
            ExamDistributionResponse response = createMockResponse(
                    Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), 0
            );
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("점수 분포 조회 성공 테스트")
    @WithMockUser(roles = "TEACHER")
    class GetDistributionSuccessTest {

        @Test
        @DisplayName("응시자가 있을 때 올바른 분포 데이터를 반환해야 한다")
        void getDistribution_success_withSubmissions() throws Exception {
            // given
            List<Integer> counts = Arrays.asList(1, 2, 3, 5, 8, 10, 12, 15, 8, 3);
            ExamDistributionResponse response = createMockResponse(counts, 67);
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ranges").isArray())
                    .andExpect(jsonPath("$.ranges.length()").value(10))
                    .andExpect(jsonPath("$.counts").isArray())
                    .andExpect(jsonPath("$.counts.length()").value(10))
                    .andExpect(jsonPath("$.totalSubmissions").value(67));
        }

        @Test
        @DisplayName("응시자가 없을 때 빈 분포 데이터를 반환해야 한다")
        void getDistribution_success_noSubmissions() throws Exception {
            // given
            List<Integer> counts = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            ExamDistributionResponse response = createMockResponse(counts, 0);
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ranges").isArray())
                    .andExpect(jsonPath("$.ranges.length()").value(10))
                    .andExpect(jsonPath("$.counts").isArray())
                    .andExpect(jsonPath("$.totalSubmissions").value(0));
        }

        @Test
        @DisplayName("올바른 점수 구간을 반환해야 한다")
        void getDistribution_success_correctRanges() throws Exception {
            // given
            List<Integer> counts = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            ExamDistributionResponse response = createMockResponse(counts, 0);
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ranges[0]").value("0-10"))
                    .andExpect(jsonPath("$.ranges[1]").value("10-20"))
                    .andExpect(jsonPath("$.ranges[2]").value("20-30"))
                    .andExpect(jsonPath("$.ranges[3]").value("30-40"))
                    .andExpect(jsonPath("$.ranges[4]").value("40-50"))
                    .andExpect(jsonPath("$.ranges[5]").value("50-60"))
                    .andExpect(jsonPath("$.ranges[6]").value("60-70"))
                    .andExpect(jsonPath("$.ranges[7]").value("70-80"))
                    .andExpect(jsonPath("$.ranges[8]").value("80-90"))
                    .andExpect(jsonPath("$.ranges[9]").value("90-100"));
        }

        @Test
        @DisplayName("특정 점수대에 집중된 분포를 올바르게 반환해야 한다")
        void getDistribution_success_concentratedScores() throws Exception {
            // given - 70-80점대에 집중
            List<Integer> counts = Arrays.asList(0, 0, 0, 0, 0, 2, 5, 20, 8, 3);
            ExamDistributionResponse response = createMockResponse(counts, 38);
            when(examStatisticsService.getScoreDistribution(EXAM_ID)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", EXAM_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.counts[7]").value(20))  // 70-80 구간
                    .andExpect(jsonPath("$.totalSubmissions").value(38));
        }
    }

    @Nested
    @DisplayName("점수 분포 조회 실패 테스트")
    @WithMockUser(roles = "TEACHER")
    class GetDistributionFailTest {

        @Test
        @DisplayName("존재하지 않는 시험 ID로 조회 시 404 Not Found 반환해야 한다")
        void getDistribution_fail_examNotFound() throws Exception {
            // given
            Long nonExistentExamId = 99999L;
            when(examStatisticsService.getScoreDistribution(nonExistentExamId))
                    .thenThrow(new NotFoundException("시험을 찾을수없습니다. ID: " + nonExistentExamId));

            // when & then
            mockMvc.perform(get("/api/exams/{examId}/distribution", nonExistentExamId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
