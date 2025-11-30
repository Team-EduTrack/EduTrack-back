package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.dto.ExamStartResponse;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.exam.entity.ExamStudentId;
import com.edutrack.domain.exam.repository.ExamRepository;

import com.edutrack.domain.exam.repository.ExamStudentRepository;
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
}
