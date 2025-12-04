package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.entity.StudentExamStatus;
import com.edutrack.domain.exam.repository.ExamStudentAnswerRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 시험 채점 서비스
 * - 기본 채점 로직 (객관식)
 * - 채점 결과 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExamGradingService {

    private final ExamStudentRepository examStudentRepository;
    private final ExamStudentAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    /**
     * 특정 학생의 시험 채점
     * - 제출된 시험만 채점 가능
     * - 각 문제별로 정답 여부 판정 및 점수 계산
     */
    public int gradeExam(Long examId, Long studentId) {
        // 응시 기록 조회
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 기록을 찾을 수 없습니다."));

        // 제출 상태 확인
        if (examStudent.getStatus() != StudentExamStatus.SUBMITTED) {
            throw new ForbiddenException("제출된 시험만 채점할 수 있습니다. 현재 상태: " + examStudent.getStatus());
        }

        // 문제 목록 조회
        List<Question> questions = questionRepository.findByExamId(examId);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 답안 목록 조회
        List<ExamStudentAnswer> answers = answerRepository.findAllByExamStudent(examStudent);

        // 채점 수행
        int totalEarnedScore = 0;
        for (ExamStudentAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestion().getId());
            if (question == null) {
                continue;
            }

            // 정답 여부 판정
            boolean isCorrect = answer.getSubmittedAnswerNumber().equals(question.getAnswerNumber());
            int earnedScore = isCorrect ? question.getScore() : 0;

            // 채점 결과 저장
            answer.mark(isCorrect, earnedScore);
            totalEarnedScore += earnedScore;
        }

        // 최종 점수 저장 및 상태 변경
        examStudent.complete(totalEarnedScore);

        return totalEarnedScore;
    }

    /**
     * 시험의 모든 제출된 답안 일괄 채점
     * - 관리자/선생님이 사용
     */
    public void gradeAllSubmissions(Long examId) {
        // 해당 시험의 모든 제출된 응시 기록 조회
        // (추후 필요시 구현 - 현재는 개별 채점으로 처리)
    }

    /**
     * 채점 결과 재계산
     * - 문제 정답이 변경되었을 때 사용
     */
    public int recalculateScore(Long examId, Long studentId) {
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 기록을 찾을 수 없습니다."));

        // 채점된 상태가 아니면 일반 채점 수행
        if (examStudent.getStatus() != StudentExamStatus.GRADED) {
            return gradeExam(examId, studentId);
        }

        // 재채점 수행
        List<Question> questions = questionRepository.findByExamId(examId);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<ExamStudentAnswer> answers = answerRepository.findAllByExamStudent(examStudent);

        int totalEarnedScore = 0;
        for (ExamStudentAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestion().getId());
            if (question == null) {
                continue;
            }

            boolean isCorrect = answer.getSubmittedAnswerNumber().equals(question.getAnswerNumber());
            int earnedScore = isCorrect ? question.getScore() : 0;

            answer.mark(isCorrect, earnedScore);
            totalEarnedScore += earnedScore;
        }

        examStudent.complete(totalEarnedScore);
        return totalEarnedScore;
    }
}

