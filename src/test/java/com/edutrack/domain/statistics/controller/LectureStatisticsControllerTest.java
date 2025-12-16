package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import com.edutrack.domain.statistics.service.LectureStatisticsService;
import com.edutrack.domain.statistics.service.UnitStatisticsService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LectureStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnitStatisticsService unitStatisticsService;

    @MockitoBean
    private LectureStatisticsService lectureStatisticsService;

    private static final Long LECTURE_ID = 1L;

    @Nested
    @DisplayName("강의별 단원 정답률 조회 - 권한 검증 테스트")
    class UnitCorrectRatesAuthorizationTest {

        @Test
        @DisplayName("인증되지 않은 사용자 접근 시 403 Forbidden 반환해야 한다")
        void getUnitCorrectRates_fail_unauthenticated() throws Exception {
            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("STUDENT 역할로 접근 시 200 OK 반환해야 한다")
        void getUnitCorrectRates_success_student() throws Exception {
            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("TEACHER 역할로 접근 시 200 OK 반환해야 한다")
        void getUnitCorrectRates_success_teacher() throws Exception {
            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "PRINCIPAL")
        @DisplayName("PRINCIPAL 역할로 접근 시 200 OK 반환해야 한다")
        void getUnitCorrectRates_success_principal() throws Exception {
            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN 역할로 접근 시 200 OK 반환해야 한다")
        void getUnitCorrectRates_success_admin() throws Exception {
            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("강의별 단원 정답률 조회 - 성공 테스트")
    @WithMockUser(roles = "STUDENT")
    class UnitCorrectRatesSuccessTest {

        @Test
        @DisplayName("단원별 정답률 데이터를 올바르게 반환해야 한다")
        void getUnitCorrectRates_success_withData() throws Exception {
            List<UnitCorrectRateResponse> mockResponses = Arrays.asList(
                    new UnitCorrectRateResponse(10L, 100, 75, 75.0),
                    new UnitCorrectRateResponse(11L, 80, 56, 70.0),
                    new UnitCorrectRateResponse(12L, 90, 81, 90.0)
            );

            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(mockResponses);

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].unitId").value(10))
                    .andExpect(jsonPath("$[0].totalTryCount").value(100))
                    .andExpect(jsonPath("$[0].correctCount").value(75))
                    .andExpect(jsonPath("$[0].correctRate").value(75.0))
                    .andExpect(jsonPath("$[1].unitId").value(11))
                    .andExpect(jsonPath("$[1].correctRate").value(70.0))
                    .andExpect(jsonPath("$[2].unitId").value(12))
                    .andExpect(jsonPath("$[2].correctRate").value(90.0));
        }

        @Test
        @DisplayName("데이터가 없을 때 빈 배열을 반환해야 한다")
        void getUnitCorrectRates_success_emptyData() throws Exception {
            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("단일 단원 데이터를 올바르게 반환해야 한다")
        void getUnitCorrectRates_success_singleUnit() throws Exception {
            List<UnitCorrectRateResponse> mockResponses = Arrays.asList(
                    new UnitCorrectRateResponse(10L, 50, 40, 80.0)
            );

            when(unitStatisticsService.getAllUnitCorrectRatesByLectureId(LECTURE_ID))
                    .thenReturn(mockResponses);

            mockMvc.perform(get("/api/lectures/{lectureId}/unit-correct-rates", LECTURE_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].unitId").value(10))
                    .andExpect(jsonPath("$[0].correctRate").value(80.0));
        }
    }
}

