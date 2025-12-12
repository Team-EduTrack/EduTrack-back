package com.edutrack.domain.statistics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.edutrack.domain.statistics.dto.QuestionCorrectRateResponse;
import com.edutrack.domain.statistics.dto.StudentQuestionStatisticsResponse;
import com.edutrack.domain.statistics.service.QuestionStatisticsService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class QuestionStatisticsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  // Service는 Mock 객체로 대체
  @Mock
  private QuestionStatisticsService questionStatisticsService;

  // Mock 주입된 Controller만 실제로 테스트
  @InjectMocks
  private QuestionStatisticsController questionStatisticsController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(questionStatisticsController)
        // 컨트롤러 어드바이스 있으면 여기서 .setControllerAdvice(...) 추가
        .build();
  }

  // 🔹 Authentication 객체 생성 (principal = userId)
  private Authentication createAuth(Long userId, String role) {
    return new UsernamePasswordAuthenticationToken(
        userId,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );
  }

  // ============================================================
  // 1️⃣ 학생 개인 문항별 정답률 조회
  // ============================================================
  @Test
  @DisplayName("학생 개인 문항별 정답률 API 성공")
  void getStudentStats_success() throws Exception {

    // Mock 반환값 준비
    StudentQuestionStatisticsResponse response =
        new StudentQuestionStatisticsResponse(
            1L, "문제1", 2, 2, true, 10, 10, 1L, 1L, 100.0
        );

    // 서비스 모킹
    when(questionStatisticsService.getStudentQuestionStatistics(1L, 10L))
        .thenReturn(List.of(response));

    // principal() -> Principal.getName() = "10" 반환
    mockMvc.perform(
            get("/api/student/statistics/exams/1/questions")
                .principal(() -> "10")
        )
        .andExpect(status().isOk());
  }

  // ============================================================
  // 2️⃣ 강사용 특정 시험 문항별 정답률
  // ============================================================
  @Test
  @DisplayName("강사용 특정 시험 문항별 정답률 API 성공")
  void getExamStats_success() throws Exception {

    QuestionCorrectRateResponse response =
        new QuestionCorrectRateResponse(1L, "문제1", 10L, 7L, 70.0);

    when(questionStatisticsService.getExamQuestionCorrectRates(1L))
        .thenReturn(List.of(response));

    mockMvc.perform(
            get("/api/statistics/exams/1/questions")
                .with(authentication(createAuth(20L, "TEACHER")))
        )
        .andExpect(status().isOk());
  }

  // ============================================================
  // 3️⃣ 강의 전체 문항별 정답률
  // ============================================================
  @Test
  @DisplayName("강의 전체 문항별 정답률 API 성공")
  void getLectureStats_success() throws Exception {

    QuestionCorrectRateResponse response =
        new QuestionCorrectRateResponse(1L, "문제1", 10L, 8L, 80.0);

    when(questionStatisticsService.getLectureQuestionCorrectRates(1L))
        .thenReturn(List.of(response));

    mockMvc.perform(
            get("/api/statistics/lectures/1/questions")
                .with(authentication(createAuth(30L, "TEACHER")))
        )
        .andExpect(status().isOk());
  }

}