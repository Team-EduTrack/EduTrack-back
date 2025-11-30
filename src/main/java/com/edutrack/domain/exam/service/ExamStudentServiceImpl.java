package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.dto.ExamStartResponse;
import com.edutrack.domain.exam.dto.ExamSubmitRequest;
import com.edutrack.domain.exam.dto.ExamSubmitResponse;
import com.edutrack.domain.exam.entity.*;
import com.edutrack.domain.exam.repository.ExamRepository;


import com.edutrack.domain.exam.entity.ExamStudentId;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.ExamStudentAnswerRepository;

import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
import com.edutrack.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamStudentServiceImpl implements ExamStudentService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final ExamStudentRepository examStudentRepository;
    private final ExamStudentAnswerRepository examStudentAnswerRepository;

    @Override
    public ExamStartResponse startExam(Long examId, Long studentId) {
        // 시험 존재 여부 검증
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new NotFoundException("시험을 찾을 수 없습니다. examId =" + examId));


        // 시험 상태 검증 (PUBLISHED 상태만 응시 가능)
        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new ConflictException("현재 상태에서는 시험을 시작 할 수 없습니다. status = " + exam.getStatus());
        }

        // 학생 조회
        User student = userRepository.findById(studentId).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. userId = " + studentId));

        // 이미 응시한 시험 (중복 방지)
        ExamStudentId id = new ExamStudentId(examId, studentId);
        if (examStudentRepository.existsById(id)) {
            throw new ConflictException("이미 응시를 한 시험입니다. examId = " + examId + ", userId = " + studentId);
        }

        // examStudent 생성 및 저장
        ExamStudent examStudent = new ExamStudent(exam, student);
        examStudentRepository.save(examStudent);

        log.info("학생 시험 응시 시작 - examId={}, studentId={}", examId, studentId);

        // 응답 DTO 구성
        return ExamStartResponse.builder()
                .examId(examId)
                .studentId(studentId)
                .startedAt(examStudent.getStartedAt())
                .status(examStudent.getStatus())
                .build();
    }

    // 시험 제출 및 채점
    @Override
    public ExamSubmitResponse submitExam(Long examId, Long studentId, ExamSubmitRequest request) {
        // 시험 조회
        Exam exam = examRepository.findById(examId).orElseThrow(()
                -> new IllegalStateException("시험을 찾을 수 없습니다. examId = " + examId));

        // 학생 조회
        User student = userRepository.findById(studentId).orElseThrow(()
                -> new IllegalStateException("학생을 찾을 수 없습니다 userId = " + studentId));

        // ExamStudent 조회
        ExamStudentId id = new ExamStudentId(examId, studentId);
        ExamStudent examStudent = examStudentRepository.findById(id).orElseThrow(()
                -> new IllegalStateException("시험 응시 기록이 없습니다. startExam 먼저 호출해야 합니다."));

        int totalScore = 0;
        int correctCount = 0;
        int wrongCount = 0;

        // 각 문항 채점
        for (ExamSubmitRequest.AnswerRequest answerRequest : request.getAnswers()) {
            Long questionId = answerRequest.getQuestionId();
            Integer submittedNum = answerRequest.getSubmittedAnswerNumber();

            // 문항 조회 ( 시험에 포함된 문제여부 )
            Question question = exam.getQuestions().stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("해당 문항을 찾을 수 없습니다. questionId = " + questionId));

            boolean isCorrect = question.getAnswerNumber().equals(submittedNum);
            int earned = isCorrect ? question.getScore() : 0;

            // ExamStudentAnswer 생성 (create 사용)
            ExamStudentAnswer examStudentAnswer = ExamStudentAnswer.create(
                    examStudent,
                    question,
                    submittedNum
            );

            // 채점 정보 세팅 (mark 사용)
            examStudentAnswer.mark(isCorrect, earned);

            // 저장
            examStudentAnswerRepository.save(examStudentAnswer);

            // 점수 누적
            if (isCorrect) {
                correctCount++;
                totalScore += earned;
            } else {
                wrongCount++;
            }

        }

        // 학생 총점 저장
        examStudent.complete(totalScore);
        examStudentRepository.save(examStudent);

        log.info("시험 제출 완료 - examId={}, studentId={}, totalScore={}",
                examId, studentId, totalScore);

        return ExamSubmitResponse.builder()
                .examId(examId)
                .studentId(studentId)
                .totalScore(totalScore)
                .correctCount(correctCount)
                .wrongCount(wrongCount)
                .build();

    }


}
