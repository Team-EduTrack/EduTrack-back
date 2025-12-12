package com.edutrack.domain.statistics.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.repository.ExamStudentAnswerRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.domain.statistics.dto.QuestionCorrectRateResponse;
import com.edutrack.domain.statistics.dto.StudentQuestionStatisticsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 문항별 정답률 통계 Service - 학생 개인 + 특정 시험 - 특정 시험 전체 - 강의 전체 시험
 */

@Service
@RequiredArgsConstructor
public class QuestionStatisticsService {

  private final QuestionRepository questionRepository;
  private final ExamStudentRepository examStudentRepository;
  private final ExamStudentAnswerRepository examStudentAnswerRepository;

  private static final double PERCENTAGE_MULTIPLIER = 100.0;

  // 1. 학생 개인 + 특정 시험 문항별 정답률
  public List<StudentQuestionStatisticsResponse> getStudentQuestionStatistics(
      Long examId, Long studentId) {

    // 학생이 시험을 봤는지 검증
    ExamStudent examStudent = examStudentRepository
        .findByExamIdAndStudentId(examId, studentId)
        .orElseThrow(() -> new IllegalArgumentException("시험 응시 정보가 없습니다."));

    // 시험 문제 목록
    List<Question> questions = questionRepository.findByExamId(examId);

    // 학생 개인 답안
    List<ExamStudentAnswer> studentAnswers =
        examStudentAnswerRepository.findAllByExamStudentAndQuestionIn(
            examStudent, questions);

    // 전체 학생 답안
    List<ExamStudentAnswer> allAnswers =
        examStudentAnswerRepository.findAllByQuestionIn(questions);

    // 학생 개인 답안 Map (문항 ID → 학생 답안)
    Map<Long, ExamStudentAnswer> studentAnswerMap = new HashMap<>();
    for (ExamStudentAnswer answer : studentAnswers) {
      studentAnswerMap.put(answer.getQuestion().getId(), answer);
    }

    // 전체 학생 통계용 Map
    Map<Long, Long> totalCountMap = new HashMap<>();
    Map<Long, Long> correctCountMap = new HashMap<>();

    for (ExamStudentAnswer answer : allAnswers) {
      Long questionId = answer.getQuestion().getId();

      totalCountMap.put(questionId, totalCountMap.getOrDefault(questionId, 0L) + 1);

      if (answer.isCorrect()) {
        correctCountMap.put(questionId, correctCountMap.getOrDefault(questionId, 0L) + 1);
      }
    }

    // DTO 변환
    List<StudentQuestionStatisticsResponse> results = new ArrayList<>();

    for (Question question : questions) {
      Long questionId = question.getId();
      ExamStudentAnswer myAnswer = studentAnswerMap.get(questionId);

      long total = totalCountMap.getOrDefault(questionId, 0L);
      long correct = correctCountMap.getOrDefault(questionId, 0L);
      double rate = total == 0 ? 0.0 : (correct * PERCENTAGE_MULTIPLIER / total);

      results.add(new StudentQuestionStatisticsResponse(
          questionId,
          question.getContent(),
          question.getAnswerNumber(),
          myAnswer != null ? myAnswer.getSubmittedAnswerNumber() : null,
          myAnswer != null && myAnswer.isCorrect(),
          myAnswer != null ? myAnswer.getEarnedScore() : 0,
          question.getScore(),
          total,
          correct,
          rate
      ));
    }

    return results;
  }

  // 2. 강사용: 특정 시험 문항별 정답률
  public List<QuestionCorrectRateResponse> getExamQuestionCorrectRates(Long examId) {

    // 문항 조회
    List<Question> questions = questionRepository.findByExamId(examId);

    // 모든 학생 답안 조회
    List<ExamStudentAnswer> answers =
        examStudentAnswerRepository.findAllByQuestionIn(questions);

    return calculateCorrectRates(questions, answers);
  }

  // 3. 강의 전체 문항별 정답률
  public List<QuestionCorrectRateResponse> getLectureQuestionCorrectRates(Long lectureId) {

    // 강의에 포함된 모든 시험의 모든 문항 조회
    List<Question> questions =
        questionRepository.findAllByExam_Lecture_Id(lectureId);

    List<ExamStudentAnswer> answers =
        examStudentAnswerRepository.findAllByQuestionIn(questions);

    return calculateCorrectRates(questions, answers);
  }

  private List<QuestionCorrectRateResponse> calculateCorrectRates(
      List<Question> questions,
      List<ExamStudentAnswer> answers
  ){

    Map<Long, Long> totalCountMap = new HashMap<>();
    Map<Long, Long> correctCountMap = new HashMap<>();

    for (ExamStudentAnswer answer : answers) {
      Long questionId = answer.getQuestion().getId();
      totalCountMap.put(questionId, totalCountMap.getOrDefault(questionId, 0L) + 1);

      if (answer.isCorrect()) {
        correctCountMap.put(questionId, correctCountMap.getOrDefault(questionId, 0L) + 1);
      }
    }

    List<QuestionCorrectRateResponse> results = new ArrayList<>();

    for (Question question : questions) {
      Long questionId = question.getId();
      long total = totalCountMap.getOrDefault(questionId, 0L);
      long correct = correctCountMap.getOrDefault(questionId, 0L);
      double rate = total == 0 ? 0 : (correct * PERCENTAGE_MULTIPLIER / total);

      results.add(new QuestionCorrectRateResponse(
          questionId,
          question.getContent(),
          total,
          correct,
          rate
      ));
    }

    return results;
  }


}
