package com.edutrack.domain.statistics.service;

import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.StudentExamStatus;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.statistics.dto.StudentAnalysisResponse;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.repository.UnitStatisticsRepository;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentReportServiceTest {

    @InjectMocks
    private StudentReportService studentReportService;

    @Mock
    private ExamStudentRepository examStudentRepository;

    @Mock
    private UnitStatisticsRepository unitStatisticsRepository;

    @Mock
    private UserRepository userRepository;

    private Long studentId;

    @BeforeEach
    void setUp() {
        studentId = 1L;
    }

    @Nested
    @DisplayName("getStudentAnalysis 메서드")
    class GetStudentAnalysis {

        @Test
        @DisplayName("성공: 학생 통합 분석 리포트를 올바르게 반환한다")
        void success_returnsStudentAnalysis() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);

            List<ExamStudent> examRecords = Arrays.asList(
                    createExamStudent(80, LocalDateTime.of(2025, 1, 1, 10, 0)),
                    createExamStudent(85, LocalDateTime.of(2025, 1, 15, 10, 0)),
                    createExamStudent(90, LocalDateTime.of(2025, 2, 1, 10, 0))
            );
            given(examStudentRepository.findAllByStudentIdWithExam(studentId)).willReturn(examRecords);

            List<StudentUnitCorrectRateResponse> weakUnits = Arrays.asList(
                    new StudentUnitCorrectRateResponse(3L, studentId, 10, 4, 40.0),
                    new StudentUnitCorrectRateResponse(5L, studentId, 8, 4, 50.0),
                    new StudentUnitCorrectRateResponse(7L, studentId, 10, 6, 60.0)
            );
            given(unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId)).willReturn(weakUnits);

            // when
            StudentAnalysisResponse response = studentReportService.getStudentAnalysis(studentId);

            // then
            assertThat(response.getAvgScore()).isEqualTo(85.0);
            assertThat(response.getUnitWeak()).containsExactly("3", "5", "7");
            assertThat(response.getTrend()).containsExactly(80, 85, 90);
        }

        @Test
        @DisplayName("성공: 시험 데이터가 없으면 빈 결과를 반환한다")
        void success_returnsEmptyWhenNoExams() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);
            given(examStudentRepository.findAllByStudentIdWithExam(studentId)).willReturn(Collections.emptyList());
            given(unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId)).willReturn(Collections.emptyList());

            // when
            StudentAnalysisResponse response = studentReportService.getStudentAnalysis(studentId);

            // then
            assertThat(response.getAvgScore()).isEqualTo(0.0);
            assertThat(response.getUnitWeak()).isEmpty();
            assertThat(response.getTrend()).isEmpty();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 학생 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenStudentNotExists() {
            // given
            given(userRepository.existsById(studentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentReportService.getStudentAnalysis(studentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("학생을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getExamSummary 메서드")
    class GetExamSummary {

        @Test
        @DisplayName("성공: 학생 시험 요약 목록을 반환한다")
        void success_returnsExamSummary() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);

            List<ExamStudent> examRecords = Arrays.asList(
                    createExamStudentWithExamInfo(1L, "중간고사", "수학", 100, 85),
                    createExamStudentWithExamInfo(2L, "기말고사", "수학", 100, 92)
            );
            given(examStudentRepository.findAllByStudentIdWithExam(studentId)).willReturn(examRecords);

            // when
            List<StudentExamSummaryResponse> response = studentReportService.getExamSummary(studentId);

            // then
            assertThat(response).hasSize(2);
            assertThat(response.get(0).getExamTitle()).isEqualTo("중간고사");
            assertThat(response.get(0).getEarnedScore()).isEqualTo(85);
            assertThat(response.get(1).getExamTitle()).isEqualTo("기말고사");
            assertThat(response.get(1).getEarnedScore()).isEqualTo(92);
        }

        @Test
        @DisplayName("성공: 채점 완료된 시험만 필터링한다")
        void success_filtersOnlyGradedExams() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);

            ExamStudent gradedExam = createExamStudentWithExamInfo(1L, "중간고사", "수학", 100, 85);
            ExamStudent inProgressExam = createExamStudentWithStatus(StudentExamStatus.IN_PROGRESS);

            given(examStudentRepository.findAllByStudentIdWithExam(studentId))
                    .willReturn(Arrays.asList(gradedExam, inProgressExam));

            // when
            List<StudentExamSummaryResponse> response = studentReportService.getExamSummary(studentId);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getExamTitle()).isEqualTo("중간고사");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 학생 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenStudentNotExists() {
            // given
            given(userRepository.existsById(studentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentReportService.getExamSummary(studentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("학생을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("getWeakUnits 메서드")
    class GetWeakUnits {

        @Test
        @DisplayName("성공: 단원별 성취도를 정답률 낮은 순으로 반환한다")
        void success_returnsWeakUnitsOrderedByCorrectRate() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);

            List<StudentUnitCorrectRateResponse> weakUnits = Arrays.asList(
                    new StudentUnitCorrectRateResponse(3L, studentId, 10, 4, 40.0),
                    new StudentUnitCorrectRateResponse(5L, studentId, 8, 4, 50.0),
                    new StudentUnitCorrectRateResponse(7L, studentId, 10, 6, 60.0),
                    new StudentUnitCorrectRateResponse(9L, studentId, 10, 7, 70.0)
            );
            given(unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId)).willReturn(weakUnits);

            // when
            List<StudentUnitCorrectRateResponse> response = studentReportService.getWeakUnits(studentId, 5);

            // then
            assertThat(response).hasSize(4);
            assertThat(response.get(0).getCorrectRate()).isEqualTo(40.0);
            assertThat(response.get(1).getCorrectRate()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("성공: limit 파라미터로 결과 개수를 제한한다")
        void success_limitsResultCount() {
            // given
            given(userRepository.existsById(studentId)).willReturn(true);

            List<StudentUnitCorrectRateResponse> weakUnits = Arrays.asList(
                    new StudentUnitCorrectRateResponse(3L, studentId, 10, 4, 40.0),
                    new StudentUnitCorrectRateResponse(5L, studentId, 8, 4, 50.0),
                    new StudentUnitCorrectRateResponse(7L, studentId, 10, 6, 60.0),
                    new StudentUnitCorrectRateResponse(9L, studentId, 10, 7, 70.0),
                    new StudentUnitCorrectRateResponse(11L, studentId, 10, 8, 80.0)
            );
            given(unitStatisticsRepository.findAllUnitCorrectRatesByStudentId(studentId)).willReturn(weakUnits);

            // when
            List<StudentUnitCorrectRateResponse> response = studentReportService.getWeakUnits(studentId, 3);

            // then
            assertThat(response).hasSize(3);
            assertThat(response.get(0).getUnitId()).isEqualTo(3L);
            assertThat(response.get(2).getUnitId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 학생 ID로 조회 시 NotFoundException 발생")
        void fail_throwsNotFoundExceptionWhenStudentNotExists() {
            // given
            given(userRepository.existsById(studentId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> studentReportService.getWeakUnits(studentId, 5))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("학생을 찾을 수 없습니다");
        }
    }

    // === Helper Methods ===

    private ExamStudent createExamStudent(int earnedScore, LocalDateTime submittedAt) {
        ExamStudent examStudent = new ExamStudent();
        ReflectionTestUtils.setField(examStudent, "earnedScore", earnedScore);
        ReflectionTestUtils.setField(examStudent, "status", StudentExamStatus.GRADED);
        ReflectionTestUtils.setField(examStudent, "submittedAt", submittedAt);
        return examStudent;
    }

    private ExamStudent createExamStudentWithExamInfo(Long examId, String examTitle, String lectureName, int totalScore, int earnedScore) {
        Lecture lecture = Lecture.builder().title(lectureName).build();

        Exam exam = new Exam();
        ReflectionTestUtils.setField(exam, "id", examId);
        ReflectionTestUtils.setField(exam, "title", examTitle);
        ReflectionTestUtils.setField(exam, "totalScore", totalScore);
        ReflectionTestUtils.setField(exam, "lecture", lecture);

        ExamStudent examStudent = new ExamStudent();
        ReflectionTestUtils.setField(examStudent, "exam", exam);
        ReflectionTestUtils.setField(examStudent, "earnedScore", earnedScore);
        ReflectionTestUtils.setField(examStudent, "status", StudentExamStatus.GRADED);
        ReflectionTestUtils.setField(examStudent, "submittedAt", LocalDateTime.now());

        return examStudent;
    }

    private ExamStudent createExamStudentWithStatus(StudentExamStatus status) {
        ExamStudent examStudent = new ExamStudent();
        ReflectionTestUtils.setField(examStudent, "status", status);
        return examStudent;
    }
}
