package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.dto.*;
import com.edutrack.domain.exam.entity.*;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentAnswerRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 학생 시험 서비스
 * - 시험 응시 시작, 답안 저장/제출, 결과 조회
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudentExamService {

    private final ExamRepository examRepository;
    private final ExamStudentRepository examStudentRepository;
    private final ExamStudentAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final LectureStudentRepository lectureStudentRepository;

    /**
     * 시험 응시 시작
     * - 학생이 시험을 시작하면 ExamStudent 레코드 생성
     * - 이미 시작한 경우 기존 정보 반환
     */
    public ExamStartResponse startExam(Long examId, Long studentId) {
        // 시험 조회
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("시험을 찾을 수 없습니다. ID: " + examId));

        // 학생 조회
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId));

        // 해당 강의 수강생인지 확인
        validateLectureEnrollment(exam.getLecture().getId(), studentId);

        // 시험 응시 가능 상태 확인
        validateExamAvailability(exam);

        // 시험 응시 기록 조회 또는 생성
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseGet(() -> {
                    ExamStudent newExamStudent = new ExamStudent(exam, student);
                    return examStudentRepository.save(newExamStudent);
                });

        // 이미 제출한 경우 예외
        if (examStudent.isSubmitted()) {
            throw new AlreadySubmittedException("이미 제출한 시험입니다.");
        }

        // 문제 목록 조회 (보기 포함)
        List<Question> questions = questionRepository.findByExamIdWithChoices(examId);

        // 개인별 마감 시간 계산
        LocalDateTime personalDeadline = calculatePersonalDeadline(exam, examStudent);

        return buildExamStartResponse(exam, examStudent, questions, personalDeadline);
    }

    /**
     * 답안 저장 (자동 저장 또는 수동 저장)
     * - 시험 제출 전에 답안을 임시 저장
     */
    public AnswerSaveResponse saveAnswers(Long examId, Long studentId, AnswerSaveRequest request) {
        // 응시 기록 확인
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 기록을 찾을 수 없습니다. 먼저 시험을 시작해주세요."));

        // 이미 제출한 경우 저장 불가
        if (examStudent.isSubmitted()) {
            throw new AlreadySubmittedException("이미 제출한 시험입니다. 답안을 수정할 수 없습니다.");
        }

        // 시간 초과 확인
        validateTimeLimit(examStudent.getExam(), examStudent);

        // 문제 ID -> Question 매핑
        List<Long> questionIds = request.getAnswers().stream()
                .map(AnswerSubmitRequest::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = questionRepository.findAllById(questionIds);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int savedCount = 0;
        for (AnswerSubmitRequest answerReq : request.getAnswers()) {
            Question question = questionMap.get(answerReq.getQuestionId());
            if (question == null) {
                continue; // 존재하지 않는 문제는 스킵
            }

            // 기존 답안 조회 또는 신규 생성
            ExamStudentAnswer answer = answerRepository
                    .findByExamStudentAndQuestion(examStudent, question)
                    .orElseGet(() -> ExamStudentAnswer.create(examStudent, question, answerReq.getSelectedAnswerNumber()));

            // 답안 업데이트 (기존 답안이 있으면 수정)
            if (answer.getId() != null) {
                // 기존 답안 수정 - 새로운 답안으로 교체
                answerRepository.delete(answer);
                answer = ExamStudentAnswer.create(examStudent, question, answerReq.getSelectedAnswerNumber());
            }

            answerRepository.save(answer);
            savedCount++;
        }

        return AnswerSaveResponse.builder()
                .examId(examId)
                .studentId(studentId)
                .savedCount(savedCount)
                .savedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 시험 제출
     * - 제출 시 상태를 SUBMITTED로 변경
     * - 자동 채점은 별도 로직에서 처리 (기존 틀 유지)
     */
    public ExamSubmitResponse submitExam(Long examId, Long studentId) {
        // 응시 기록 확인
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 기록을 찾을 수 없습니다."));

        Exam exam = examStudent.getExam();

        // 이미 제출한 경우
        if (examStudent.isSubmitted()) {
            throw new AlreadySubmittedException("이미 제출한 시험입니다.");
        }

        // 시험 종료 상태 확인
        if (exam.getStatus() == ExamStatus.CLOSED) {
            throw new ExamClosedException("종료된 시험에는 제출할 수 없습니다.");
        }

        // 시험 마감 시간 확인
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndDate())) {
            throw new ExamDeadlineExceededException("시험 마감 시간이 지났습니다. 마감 시간: " + exam.getEndDate());
        }

        // 제출 처리
        examStudent.submit();

        return ExamSubmitResponse.builder()
                .examId(examId)
                .studentId(studentId)
                .submittedAt(examStudent.getSubmittedAt())
                .message("시험이 성공적으로 제출되었습니다.")
                .build();
    }

    /**
     * 시험 결과 조회
     * - 채점 완료된 시험만 결과 조회 가능
     */
    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(Long examId, Long studentId) {
        // 응시 기록 확인
        ExamStudent examStudent = examStudentRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 기록을 찾을 수 없습니다."));

        // 채점 완료 여부 확인
        if (examStudent.getStatus() != StudentExamStatus.GRADED) {
            throw new ForbiddenException("아직 채점이 완료되지 않았습니다.");
        }

        Exam exam = examStudent.getExam();
        User student = examStudent.getStudent();

        // 답안 목록 조회
        List<ExamStudentAnswer> answers = answerRepository
                .findAllByExamIdAndStudentIdWithQuestion(examId, studentId);

        // 총 배점 계산
        int totalScore = questionRepository.sumScoreByExamId(examId);

        // 정답 개수 계산
        int correctCount = (int) answers.stream().filter(ExamStudentAnswer::isCorrect).count();

        // 문제별 결과 생성
        List<ExamResultResponse.QuestionResult> questionResults = answers.stream()
                .map(answer -> ExamResultResponse.QuestionResult.builder()
                        .questionId(answer.getQuestion().getId())
                        .content(answer.getQuestion().getContent())
                        .score(answer.getQuestion().getScore())
                        .submittedAnswer(answer.getSubmittedAnswerNumber())
                        .correctAnswer(answer.getQuestion().getAnswerNumber())
                        .isCorrect(answer.isCorrect())
                        .earnedScore(answer.getEarnedScore())
                        .build())
                .collect(Collectors.toList());

        return ExamResultResponse.builder()
                .examId(examId)
                .examTitle(exam.getTitle())
                .studentId(studentId)
                .studentName(student.getName())
                .status(examStudent.getStatus().name())
                .totalScore(totalScore)
                .earnedScore(examStudent.getEarnedScore())
                .correctCount(correctCount)
                .totalQuestionCount(answers.size())
                .startedAt(examStudent.getStartedAt())
                .submittedAt(examStudent.getSubmittedAt())
                .questionResults(questionResults)
                .build();
    }

    /**
     * 학생의 시험 기록 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ExamRecordResponse> getExamRecords(Long studentId) {
        List<ExamStudent> examStudents = examStudentRepository.findAllByStudentIdWithExam(studentId);

        return examStudents.stream()
                .map(es -> {
                    Exam exam = es.getExam();
                    int totalScore = questionRepository.sumScoreByExamId(exam.getId());

                    return ExamRecordResponse.builder()
                            .examId(exam.getId())
                            .examTitle(exam.getTitle())
                            .lectureName(exam.getLecture().getTitle())
                            .status(es.getStatus().name())
                            .totalScore(totalScore)
                            .earnedScore(es.getEarnedScore())
                            .startedAt(es.getStartedAt())
                            .submittedAt(es.getSubmittedAt())
                            .examStartDate(exam.getStartDate())
                            .examEndDate(exam.getEndDate())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // === Private 메서드 ===

    /**
     * 강의 수강 여부 확인
     */
    private void validateLectureEnrollment(Long lectureId, Long studentId) {
        LectureStudentId id = new LectureStudentId(lectureId, studentId);
        if (!lectureStudentRepository.existsById(id)) {
            throw new ForbiddenException("해당 강의를 수강하지 않는 학생입니다.");
        }
    }

    /**
     * 시험 응시 가능 상태 확인
     */
    private void validateExamAvailability(Exam exam) {
        if (exam.getStatus() != ExamStatus.PUBLISHED) {
            throw new ForbiddenException("현재 응시할 수 없는 시험입니다. 시험 상태: " + exam.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartDate())) {
            throw new ForbiddenException("시험 시작 시간이 아직 되지 않았습니다. 시작 시간: " + exam.getStartDate());
        }

        if (now.isAfter(exam.getEndDate())) {
            throw new ExamDeadlineExceededException("시험 마감 시간이 지났습니다. 마감 시간: " + exam.getEndDate());
        }
    }

    /**
     * 시간 제한 확인
     */
    private void validateTimeLimit(Exam exam, ExamStudent examStudent) {
        LocalDateTime now = LocalDateTime.now();

        // 시험 전체 마감 시간 확인
        if (now.isAfter(exam.getEndDate())) {
            throw new ExamDeadlineExceededException("시험 마감 시간이 지났습니다.");
        }

        // 개인별 제한 시간 확인
        if (exam.getDurationMinute() != null) {
            LocalDateTime personalDeadline = examStudent.getStartedAt()
                    .plusMinutes(exam.getDurationMinute());
            if (now.isAfter(personalDeadline)) {
                throw new ExamDeadlineExceededException("시험 제한 시간이 초과되었습니다.");
            }
        }
    }

    /**
     * 개인별 마감 시간 계산
     */
    private LocalDateTime calculatePersonalDeadline(Exam exam, ExamStudent examStudent) {
        if (exam.getDurationMinute() == null) {
            return exam.getEndDate();
        }

        LocalDateTime personalDeadline = examStudent.getStartedAt()
                .plusMinutes(exam.getDurationMinute());

        // 개인별 마감과 시험 전체 마감 중 빠른 시간 반환
        return personalDeadline.isBefore(exam.getEndDate()) ? personalDeadline : exam.getEndDate();
    }

    /**
     * 시험 시작 응답 생성
     */
    private ExamStartResponse buildExamStartResponse(Exam exam, ExamStudent examStudent,
                                                      List<Question> questions, LocalDateTime personalDeadline) {
        List<ExamStartResponse.QuestionForStudent> questionDtos = questions.stream()
                .map(q -> ExamStartResponse.QuestionForStudent.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .score(q.getScore())
                        .difficulty(q.getDifficulty().name())
                        .choices(q.getChoices().stream()
                                .map(c -> ExamStartResponse.ChoiceForStudent.builder()
                                        .choiceId(c.getId())
                                        .content(c.getContent())
                                        .choiceNumber(c.getChoiceNumber())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return ExamStartResponse.builder()
                .examId(exam.getId())
                .studentId(examStudent.getStudent().getId())
                .title(exam.getTitle())
                .startDate(exam.getStartDate())
                .endDate(exam.getEndDate())
                .durationMinute(exam.getDurationMinute())
                .startedAt(examStudent.getStartedAt())
                .examStartedAt(examStudent.getStartedAt())
                .personalDeadline(personalDeadline)
                .status(examStudent.getStatus())
                .questions(questionDtos)
                .build();
    }
}

