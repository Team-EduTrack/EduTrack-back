package com.edutrack.domain.exam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.dto.*;
import com.edutrack.domain.exam.entity.*;
import com.edutrack.domain.exam.repository.*;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 학생 시험 서비스 테스트
 * - 시험 응시 시작, 답안 저장, 시험 제출, 결과 조회
 */
@ExtendWith(MockitoExtension.class)
class StudentExamServiceTest {

    private static final Logger log = LoggerFactory.getLogger(StudentExamServiceTest.class);

    @InjectMocks
    private StudentExamService studentExamService;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamStudentRepository examStudentRepository;

    @Mock
    private ExamStudentAnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LectureStudentRepository lectureStudentRepository;

    private User student;
    private Exam exam;
    private Lecture lecture;
    private Long studentId;
    private Long examId;
    private Long lectureId;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        examId = 100L;
        lectureId = 10L;

        // Student Mock
        student = mock(User.class);
        lenient().when(student.getId()).thenReturn(studentId);
        lenient().when(student.getName()).thenReturn("홍길동");

        // Lecture Mock
        lecture = mock(Lecture.class);
        lenient().when(lecture.getId()).thenReturn(lectureId);
        lenient().when(lecture.getTitle()).thenReturn("수학 기초");

        // Exam Mock
        exam = mock(Exam.class);
        lenient().when(exam.getId()).thenReturn(examId);
        lenient().when(exam.getTitle()).thenReturn("중간고사");
        lenient().when(exam.getLecture()).thenReturn(lecture);
        lenient().when(exam.getStatus()).thenReturn(ExamStatus.PUBLISHED);
        lenient().when(exam.getStartDate()).thenReturn(LocalDateTime.now().minusHours(1));
        lenient().when(exam.getEndDate()).thenReturn(LocalDateTime.now().plusHours(2));
        lenient().when(exam.getDurationMinute()).thenReturn(60);
    }

    @Test
    @DisplayName("시험 응시 시작 성공")
    void 시험_응시_시작_성공() {
        // given
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(lectureStudentRepository.existsById(any(LectureStudentId.class))).thenReturn(true);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.empty());

        ExamStudent newExamStudent = mock(ExamStudent.class);
        when(newExamStudent.getStartedAt()).thenReturn(LocalDateTime.now());
        when(newExamStudent.isSubmitted()).thenReturn(false);
        when(newExamStudent.getStudent()).thenReturn(student);
        when(newExamStudent.getStatus()).thenReturn(StudentExamStatus.IN_PROGRESS);
        when(examStudentRepository.save(any(ExamStudent.class))).thenReturn(newExamStudent);

        // Question Mock
        Question question1 = createMockQuestion(1L, "1+1=?", 10, Difficulty.EASY);
        Question question2 = createMockQuestion(2L, "2+2=?", 10, Difficulty.MEDIUM);
        when(questionRepository.findByExamIdWithChoices(examId))
                .thenReturn(List.of(question1, question2));

        // when
        ExamStartResponse result = studentExamService.startExam(examId, studentId);

        // then
        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals("중간고사", result.getTitle());
        assertEquals(2, result.getQuestions().size());

        verify(examRepository).findById(examId);
        verify(userRepository).findById(studentId);
        verify(lectureStudentRepository).existsById(any(LectureStudentId.class));

        log.info("=== 시험 응시 시작 테스트 결과 ===");
        log.info("시험 ID: {}, 제목: {}, 문제 수: {}",
                result.getExamId(), result.getTitle(), result.getQuestions().size());
    }

    @Test
    @DisplayName("시험 응시 시작 실패 - 수강하지 않는 강의")
    void 시험_응시_시작_실패_수강하지_않는_강의() {
        // given
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(lectureStudentRepository.existsById(any(LectureStudentId.class))).thenReturn(false);

        // when & then
        assertThrows(ForbiddenException.class, () -> {
            studentExamService.startExam(examId, studentId);
        });

        log.info("=== 수강하지 않는 강의 시험 응시 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 응시 시작 실패 - 이미 제출한 시험")
    void 시험_응시_시작_실패_이미_제출() {
        // given
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(lectureStudentRepository.existsById(any(LectureStudentId.class))).thenReturn(true);

        ExamStudent submittedExamStudent = mock(ExamStudent.class);
        when(submittedExamStudent.isSubmitted()).thenReturn(true);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(submittedExamStudent));

        // when & then
        assertThrows(AlreadySubmittedException.class, () -> {
            studentExamService.startExam(examId, studentId);
        });

        log.info("=== 이미 제출한 시험 재응시 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("답안 저장 성공")
    void 답안_저장_성공() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.isSubmitted()).thenReturn(false);
        when(examStudent.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(examStudent.getExam()).thenReturn(exam);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        Question question = createMockQuestion(1L, "1+1=?", 10, Difficulty.EASY);
        when(questionRepository.findAllById(anyList())).thenReturn(List.of(question));
        when(answerRepository.findByExamStudentAndQuestion(any(), any()))
                .thenReturn(Optional.empty());

        AnswerSaveRequest request = new AnswerSaveRequest();
        AnswerSubmitRequest answerReq = new AnswerSubmitRequest();
        answerReq.setQuestionId(1L);
        answerReq.setSelectedAnswerNumber(2);
        request.setAnswers(List.of(answerReq));

        // when
        AnswerSaveResponse result = studentExamService.saveAnswers(examId, studentId, request);

        // then
        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(1, result.getSavedCount());

        verify(answerRepository).save(any(ExamStudentAnswer.class));

        log.info("=== 답안 저장 테스트 결과 ===");
        log.info("시험 ID: {}, 저장된 답안 수: {}", result.getExamId(), result.getSavedCount());
    }

    @Test
    @DisplayName("시험 제출 성공")
    void 시험_제출_성공() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.isSubmitted()).thenReturn(false);
        when(examStudent.getExam()).thenReturn(exam);
        when(examStudent.getSubmittedAt()).thenReturn(LocalDateTime.now());
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // when
        ExamSubmitResponse result = studentExamService.submitExam(examId, studentId);

        // then
        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals(studentId, result.getStudentId());
        assertNotNull(result.getMessage());

        verify(examStudent).submit();

        log.info("=== 시험 제출 테스트 결과 ===");
        log.info("시험 ID: {}, 학생 ID: {}, 메시지: {}",
                result.getExamId(), result.getStudentId(), result.getMessage());
    }

    @Test
    @DisplayName("시험 제출 실패 - 이미 제출한 시험")
    void 시험_제출_실패_이미_제출() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.isSubmitted()).thenReturn(true);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // when & then
        assertThrows(AlreadySubmittedException.class, () -> {
            studentExamService.submitExam(examId, studentId);
        });

        log.info("=== 이미 제출한 시험 재제출 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 제출 실패 - 종료된 시험")
    void 시험_제출_실패_종료된_시험() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.isSubmitted()).thenReturn(false);

        Exam closedExam = mock(Exam.class);
        when(closedExam.getStatus()).thenReturn(ExamStatus.CLOSED);
        when(examStudent.getExam()).thenReturn(closedExam);

        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // when & then
        assertThrows(ExamClosedException.class, () -> {
            studentExamService.submitExam(examId, studentId);
        });

        log.info("=== 종료된 시험 제출 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 결과 조회 성공")
    void 시험_결과_조회_성공() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.getStatus()).thenReturn(StudentExamStatus.GRADED);
        when(examStudent.getExam()).thenReturn(exam);
        when(examStudent.getStudent()).thenReturn(student);
        when(examStudent.getEarnedScore()).thenReturn(85);
        when(examStudent.getStartedAt()).thenReturn(LocalDateTime.now().minusHours(1));
        when(examStudent.getSubmittedAt()).thenReturn(LocalDateTime.now().minusMinutes(30));
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // 답안 Mock
        Question question = createMockQuestion(1L, "1+1=?", 10, Difficulty.EASY);
        ExamStudentAnswer answer = mock(ExamStudentAnswer.class);
        when(answer.getQuestion()).thenReturn(question);
        when(answer.getSubmittedAnswerNumber()).thenReturn(2);
        when(answer.isCorrect()).thenReturn(true);
        when(answer.getEarnedScore()).thenReturn(10);

        when(answerRepository.findAllByExamIdAndStudentIdWithQuestion(examId, studentId))
                .thenReturn(List.of(answer));
        when(questionRepository.sumScoreByExamId(examId)).thenReturn(100);

        // when
        ExamResultResponse result = studentExamService.getExamResult(examId, studentId);

        // then
        assertNotNull(result);
        assertEquals(examId, result.getExamId());
        assertEquals("중간고사", result.getExamTitle());
        assertEquals(85, result.getEarnedScore());
        assertEquals(100, result.getTotalScore());
        assertEquals(1, result.getCorrectCount());

        log.info("=== 시험 결과 조회 테스트 결과 ===");
        log.info("시험: {}, 총점: {}, 획득 점수: {}, 정답 수: {}",
                result.getExamTitle(), result.getTotalScore(),
                result.getEarnedScore(), result.getCorrectCount());
    }

    @Test
    @DisplayName("시험 결과 조회 실패 - 채점 미완료")
    void 시험_결과_조회_실패_채점_미완료() {
        // given
        ExamStudent examStudent = mock(ExamStudent.class);
        when(examStudent.getStatus()).thenReturn(StudentExamStatus.SUBMITTED);
        when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
                .thenReturn(Optional.of(examStudent));

        // when & then
        assertThrows(ForbiddenException.class, () -> {
            studentExamService.getExamResult(examId, studentId);
        });

        log.info("=== 채점 미완료 시험 결과 조회 시 예외 발생 확인 ===");
    }

    @Test
    @DisplayName("시험 기록 목록 조회 성공")
    void 시험_기록_목록_조회_성공() {
        // given
        ExamStudent examStudent1 = mock(ExamStudent.class);
        when(examStudent1.getExam()).thenReturn(exam);
        when(examStudent1.getStatus()).thenReturn(StudentExamStatus.GRADED);
        when(examStudent1.getEarnedScore()).thenReturn(85);
        when(examStudent1.getStartedAt()).thenReturn(LocalDateTime.now().minusDays(7));
        when(examStudent1.getSubmittedAt()).thenReturn(LocalDateTime.now().minusDays(7));

        when(examStudentRepository.findAllByStudentIdWithExam(studentId))
                .thenReturn(List.of(examStudent1));
        when(questionRepository.sumScoreByExamId(examId)).thenReturn(100);

        // when
        List<ExamRecordResponse> result = studentExamService.getExamRecords(studentId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(examId, result.get(0).getExamId());
        assertEquals("중간고사", result.get(0).getExamTitle());
        assertEquals(85, result.get(0).getEarnedScore());

        log.info("=== 시험 기록 목록 조회 테스트 결과 ===");
        result.forEach(r -> log.info(
                "시험: {}, 상태: {}, 점수: {}/{}",
                r.getExamTitle(), r.getStatus(), r.getEarnedScore(), r.getTotalScore()
        ));
    }

    // === Helper Methods ===

    private Question createMockQuestion(Long id, String content, int score, Difficulty difficulty) {
        Question question = mock(Question.class);
        when(question.getId()).thenReturn(id);
        lenient().when(question.getContent()).thenReturn(content);
        lenient().when(question.getScore()).thenReturn(score);
        lenient().when(question.getDifficulty()).thenReturn(difficulty);
        lenient().when(question.getAnswerNumber()).thenReturn(2);
        lenient().when(question.getUnitId()).thenReturn(1L);  // ✨ 추가: unitId 설정

      List<Choice> choices = List.of(
          createMockChoice(1L, "보기1", 1),
          createMockChoice(2L, "보기2", 2),
          createMockChoice(3L, "보기3", 3),
          createMockChoice(4L, "보기4", 4));

      lenient().when(question.getChoices()).thenReturn(choices);
        return question;
    }

    private Choice createMockChoice(Long id, String content, int choiceNumber) {
      Choice choice = mock(Choice.class);
      lenient().when(choice.getId()).thenReturn(id);
      lenient().when(choice.getContent()).thenReturn(content);
      lenient().when(choice.getChoiceNumber()).thenReturn(choiceNumber);
      return choice;
    }
}

