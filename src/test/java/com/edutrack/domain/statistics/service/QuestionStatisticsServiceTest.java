package com.edutrack.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.repository.ExamStudentAnswerRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.domain.statistics.dto.QuestionCorrectRateResponse;
import com.edutrack.domain.statistics.dto.StudentQuestionStatisticsResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionStatisticsServiceTest {

  @InjectMocks
  private QuestionStatisticsService questionStatisticsService;

  @Mock
  private QuestionRepository questionRepository;

  @Mock
  private ExamStudentRepository examStudentRepository;

  @Mock
  private ExamStudentAnswerRepository examStudentAnswerRepository;

  private Long examId;
  private Long studentId;

  @BeforeEach
  void setUp() {
    examId = 1L;
    studentId = 10L;
  }

  // ✅ 1. 학생 개인 + 특정 시험 문항별 정답률
  @Test
  @DisplayName("학생 개인 문항별 정답률 조회 성공")
  void getStudentQuestionStatistics_success() {

    // ✅ given
    // ExamStudent mock 생성
    ExamStudent examStudent = mock(ExamStudent.class);
    // 특정 시험 + 학생 응시 정보가 존재한다고 가정
    when(examStudentRepository.findByExamIdAndStudentId(examId, studentId))
        .thenReturn(Optional.of(examStudent));

    // 문제 1개 mocking
    Question q1 = mock(
        Question.class);
    when(q1.getId()).thenReturn(1L);
    when(q1.getContent()).thenReturn("1번 문제");
    when(q1.getAnswerNumber()).thenReturn(2);
    when(q1.getScore()).thenReturn(10);

    List<Question> questions = List.of(q1);
    when(questionRepository.findByExamId(examId)).thenReturn(questions);

    // 학생 개인 답안 mocking (정답)
    ExamStudentAnswer myAnswer = mock(ExamStudentAnswer.class);
    when(myAnswer.getQuestion()).thenReturn(q1);
    when(myAnswer.getSubmittedAnswerNumber()).thenReturn(2);
    when(myAnswer.isCorrect()).thenReturn(true);
    when(myAnswer.getEarnedScore()).thenReturn(10);

    // 개인 답안, 전체 답안 리스트 반환
    when(examStudentAnswerRepository
        .findAllByExamStudentAndQuestionIn(examStudent, questions))
        .thenReturn(List.of(myAnswer));

    when(examStudentAnswerRepository.findAllByQuestionIn(questions))
        .thenReturn(List.of(myAnswer));

    // ✅ when
    List<StudentQuestionStatisticsResponse> result =
        questionStatisticsService.getStudentQuestionStatistics(examId, studentId);

    // ✅ then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getCorrectRate()).isEqualTo(100.0);
  }


  // ✅ 2. 강사용 특정 시험 문항별 정답률
  @Test
  @DisplayName("강사용 특정 시험 문항별 정답률 조회 성공")
  void getExamQuestionCorrectRates_success() {

    // given
    Question question = mock(Question.class);
    when(question.getId()).thenReturn(1L);
    when(question.getContent()).thenReturn("문제1");

    when(questionRepository.findByExamId(examId))
        .thenReturn(List.of(question));

    ExamStudentAnswer answer = mock(ExamStudentAnswer.class);
    when(answer.getQuestion()).thenReturn(question);
    when(answer.isCorrect()).thenReturn(true);

    when(examStudentAnswerRepository.findAllByQuestionIn(any()))
        .thenReturn(List.of(answer));

    // when
    List<QuestionCorrectRateResponse> result =
        questionStatisticsService.getExamQuestionCorrectRates(examId);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getCorrectRate()).isEqualTo(100.0);
  }


  // ✅ 3. 강의 전체 문항별 정답률
  @Test
  @DisplayName("강의 전체 문항별 정답률 조회 성공")
  void getLectureQuestionCorrectRates_success() {

    // given
    Long lectureId = 1L;

    Question question = mock(Question.class);
    when(question.getId()).thenReturn(1L);
    when(question.getContent()).thenReturn("문제1");

    when(questionRepository.findAllByExam_Lecture_Id(lectureId))
        .thenReturn(List.of(question));

    ExamStudentAnswer answer = mock(ExamStudentAnswer.class);
    when(answer.getQuestion()).thenReturn(question);
    when(answer.isCorrect()).thenReturn(true);

    when(examStudentAnswerRepository.findAllByQuestionIn(any()))
        .thenReturn(List.of(answer));

    // when
    List<QuestionCorrectRateResponse> result =
        questionStatisticsService.getLectureQuestionCorrectRates(lectureId);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getCorrectRate()).isEqualTo(100.0);
  }



}