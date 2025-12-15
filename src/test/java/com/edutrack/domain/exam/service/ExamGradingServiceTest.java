package com.edutrack.domain.exam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.*;
import com.edutrack.domain.exam.repository.*;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 시험 채점 서비스 테스트
 * - 객관식 자동 채점 로직
 */
@ExtendWith(MockitoExtension.class)
class ExamGradingServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ExamGradingServiceTest.class);

    @InjectMocks
    private ExamGradingService gradingService;

    @Mock
    private ExamStudentRepository examStudentRepository;

    @Mock
    private ExamStudentAnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    private Long examId;
    private Long studentId;
    private ExamStudent examStudent;

    @BeforeEach
    void setUp() {
        examId = 100L;
        studentId = 1L;

        examStudent = mock(ExamStudent.class);
        lenient().when(examStudent.getStatus()).thenReturn(StudentExamStatus.SUBMITTED);
    }

    @Test
    @DisplayName("시험 채점 성공 - 전부 정답")
    void 시험_채점_성공_전부_정답() {
        // given
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // 문제 생성 (정답: 2, 3)
        Question q1 = createQuestion(1L, 2, 10);
        Question q2 = createQuestion(2L, 3, 20);
        when(questionRepository.findByExamId(examId)).thenReturn(List.of(q1, q2));

        // 답안 생성 (제출 답안: 2, 3 -> 모두 정답)
        ExamStudentAnswer ans1 = createAnswer(q1, 2);
        ExamStudentAnswer ans2 = createAnswer(q2, 3);
        when(answerRepository.findAllByExamStudent(examStudent)).thenReturn(List.of(ans1, ans2));

        // when
        int totalScore = gradingService.gradeExam(examId, studentId);

        // then
        assertEquals(30, totalScore); // 10 + 20

        // 채점 결과 확인
        verify(ans1).mark(true, 10);
        verify(ans2).mark(true, 20);
        verify(examStudent).complete(30);

        log.info("=== 전부 정답 채점 테스트 결과 ===");
        log.info("총 획득 점수: {}", totalScore);
    }

    @Test
    @DisplayName("시험 채점 성공 - 부분 정답")
    void 시험_채점_성공_부분_정답() {
        // given
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // 문제 생성 (정답: 2, 3, 1)
        Question q1 = createQuestion(1L, 2, 10);
        Question q2 = createQuestion(2L, 3, 20);
        Question q3 = createQuestion(3L, 1, 30);
        when(questionRepository.findByExamId(examId)).thenReturn(List.of(q1, q2, q3));

        // 답안 생성 (제출 답안: 2, 1, 4 -> q1만 정답)
        ExamStudentAnswer ans1 = createAnswer(q1, 2);  // 정답
        ExamStudentAnswer ans2 = createAnswer(q2, 1);  // 오답
        ExamStudentAnswer ans3 = createAnswer(q3, 4);  // 오답
        when(answerRepository.findAllByExamStudent(examStudent)).thenReturn(List.of(ans1, ans2, ans3));

        // when
        int totalScore = gradingService.gradeExam(examId, studentId);

        // then
        assertEquals(10, totalScore); // q1만 정답

        verify(ans1).mark(true, 10);
        verify(ans2).mark(false, 0);
        verify(ans3).mark(false, 0);
        verify(examStudent).complete(10);

        log.info("=== 부분 정답 채점 테스트 결과 ===");
        log.info("총 획득 점수: {} (60점 만점 중)", totalScore);
    }

    @Test
    @DisplayName("시험 채점 성공 - 전부 오답")
    void 시험_채점_성공_전부_오답() {
        // given
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // 문제 생성 (정답: 2, 3)
        Question q1 = createQuestion(1L, 2, 10);
        Question q2 = createQuestion(2L, 3, 20);
        when(questionRepository.findByExamId(examId)).thenReturn(List.of(q1, q2));

        // 답안 생성 (제출 답안: 1, 1 -> 모두 오답)
        ExamStudentAnswer ans1 = createAnswer(q1, 1);
        ExamStudentAnswer ans2 = createAnswer(q2, 1);
        when(answerRepository.findAllByExamStudent(examStudent)).thenReturn(List.of(ans1, ans2));

        // when
        int totalScore = gradingService.gradeExam(examId, studentId);

        // then
        assertEquals(0, totalScore);

        verify(ans1).mark(false, 0);
        verify(ans2).mark(false, 0);
        verify(examStudent).complete(0);

        log.info("=== 전부 오답 채점 테스트 결과 ===");
        log.info("총 획득 점수: {}", totalScore);
    }

    @Test
    @DisplayName("시험 채점 실패 - 응시 기록 없음")
    void 시험_채점_실패_응시_기록_없음() {
        // given
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            gradingService.gradeExam(examId, studentId);
        });

        log.info("=== 응시 기록 없는 시험 채점 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 채점 실패 - 제출 전 상태")
    void 시험_채점_실패_제출_전_상태() {
        // given
        ExamStudent inProgressExamStudent = mock(ExamStudent.class);
        when(inProgressExamStudent.getStatus()).thenReturn(StudentExamStatus.IN_PROGRESS);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(inProgressExamStudent));

        // when & then
        assertThrows(ForbiddenException.class, () -> {
            gradingService.gradeExam(examId, studentId);
        });

        log.info("=== 제출 전 시험 채점 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 재채점 성공")
    void 시험_재채점_성공() {
        // given
        ExamStudent gradedExamStudent = mock(ExamStudent.class);
        when(gradedExamStudent.getStatus()).thenReturn(StudentExamStatus.GRADED);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(gradedExamStudent));

        // 문제 생성 (정답 변경: 2 -> 1)
        Question q1 = createQuestion(1L, 1, 10); // 정답이 1로 변경됨
        when(questionRepository.findByExamId(examId)).thenReturn(List.of(q1));

        // 기존 답안 (제출 답안: 1 -> 이제 정답)
        ExamStudentAnswer ans1 = createAnswer(q1, 1);
        when(answerRepository.findAllByExamStudent(gradedExamStudent)).thenReturn(List.of(ans1));

        // when
        int totalScore = gradingService.recalculateScore(examId, studentId);

        // then
        assertEquals(10, totalScore);
        verify(ans1).mark(true, 10);
        verify(gradedExamStudent).complete(10);

        log.info("=== 재채점 테스트 결과 ===");
        log.info("재채점 후 총 점수: {}", totalScore);
    }

    // === Helper Methods ===

    private Question createQuestion(Long id, int answerNumber, int score) {
        Question question = mock(Question.class);
        when(question.getId()).thenReturn(id);
        when(question.getAnswerNumber()).thenReturn(answerNumber);
        when(question.getScore()).thenReturn(score);
        return question;
    }

    private ExamStudentAnswer createAnswer(Question question, int submittedAnswer) {
        ExamStudentAnswer answer = mock(ExamStudentAnswer.class);
        when(answer.getQuestion()).thenReturn(question);
        when(answer.getSubmittedAnswerNumber()).thenReturn(submittedAnswer);
        return answer;
    }
}




