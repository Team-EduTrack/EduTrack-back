package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.dto.ExamCreationRequest;
import com.edutrack.domain.exam.dto.ExamDetailResponse;
import com.edutrack.domain.exam.dto.QuestionRegistrationRequest;
import com.edutrack.domain.exam.entity.Choice;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.AlreadySubmittedException;
import com.edutrack.global.exception.ConflictException;
import com.edutrack.global.exception.ExamClosedException;
import com.edutrack.global.exception.ExamDeadlineExceededException;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamService {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamStudentRepository examStudentRepository;

    public Long createExam(Long principalUserId, ExamCreationRequest request) {


        User principal = userRepository.findById(principalUserId)
                .orElseThrow(() -> new NotFoundException("시험 생성 권한을 가진 사용자를 찾을 수 없습니다."));


        Lecture lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new NotFoundException("지정된 강의를 찾을 수 없습니다. ID: " + request.getLectureId()));


        if (!principal.hasRole(RoleType.ADMIN)) {
            if (!lecture.getTeacher().getId().equals(principalUserId) &&
                    !principal.hasRole(RoleType.PRINCIPAL)) {
                throw new ForbiddenException("해당 강의에 대한 시험 생성 권한이 없습니다.");
            }
        }


        validateDates(request.getStartDate(), request.getEndDate());


        Exam exam = new Exam(
                lecture,
                request.getTitle(),
                ExamStatus.DRAFT,
                request.getStartDate(),
                request.getEndDate(),
                request.getDurationMinute()
        );

        Exam savedExam = examRepository.save(exam);
        return savedExam.getId();
    }



    public List<Long> registerQuestions(Long examId, List<QuestionRegistrationRequest> requests) {


        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("지정된 시험을 찾을 수 없습니다. ID: " + examId));


        List<Question> savedQuestions = requests.stream()
                .map(req -> {
                    // Question 엔티티 생성
                    Question question = Question.builder()
                            .exam(exam)
                            .content(req.getContent())
                            .answerNumber(req.getAnswerNumber())
                            .score(req.getScore())
                            .unitId(req.getUnitId())
                            .difficulty(req.getDifficulty())
                            .build();


                    IntStream.range(0, req.getChoices().size())
                            .forEach(index -> {
                                Choice choice = Choice.builder()
                                        .content(req.getChoices().get(index))
                                        .choiceNumber(index + 1) // 1부터 시작
                                        .build();
                                question.addChoice(choice);
                            });

                    return question;
                })
                .collect(Collectors.toList());


        List<Question> saved = questionRepository.saveAll(savedQuestions);

        return saved.stream().map(Question::getId).collect(Collectors.toList());
    }

    //시험 상세조회로직

    @Transactional(readOnly = true)
    public ExamDetailResponse getExamDetail(Long examId, Long principalUserId) {
        //시험 조회
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("지정된 시험을 찾을 수 없습니다. ID : "+ examId));

        if (!isExamOwner(exam, principalUserId)) {
            throw new ForbiddenException("해당 시험 상세 정보를 조회할 권한이 없습니다.");
        }

        // 문제 목록 조회
        List<Question> questions = questionRepository.findByExamId(examId);

        return ExamDetailResponse.of(exam, questions);
    }

    @Transactional(readOnly = true)
    private boolean isExamOwner(Exam exam, Long principalUserId) {
        User principal = userRepository.findById(principalUserId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));


        if (principal.hasRole(com.edutrack.domain.user.entity.RoleType.PRINCIPAL) &&
                exam.getLecture().getAcademy().getId().equals(principal.getAcademy().getId())) {
            return true;
        }

        if (principal.hasRole(com.edutrack.domain.user.entity.RoleType.TEACHER) &&
                exam.getLecture().getTeacher().getId().equals(principalUserId)) {
            return true;
        }

        return false;
    }



    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ConflictException("시험 시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    /**
     * 시험 제출
     * - 이미 제출된 시험인지 검증
     * - 시험이 종료된 상태인지 검증
     * - 시험 마감 시간이 지났는지 검증
     */
    public void submitExam(Long examId, Long studentId) {
        // 1. 시험 존재 여부 확인
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("시험을 찾을 수 없습니다. ID: " + examId));

        // 2. 학생의 시험 응시 정보 확인
        ExamStudent examStudent = examStudentRepository.findByExamIdAndStudentId(examId, studentId)
                .orElseThrow(() -> new NotFoundException("시험 응시 정보를 찾을 수 없습니다. 시험을 시작하지 않았습니다."));

        // 3. 이미 제출된 시험인지 확인 (중복 제출 방지)
        if (examStudent.isSubmitted()) {
            throw new AlreadySubmittedException("이미 제출한 시험입니다.");
        }

        // 4. 시험 상태 확인 (종료된 시험인지)
        if (exam.getStatus() == ExamStatus.CLOSED) {
            throw new ExamClosedException("종료된 시험에는 제출할 수 없습니다.");
        }

        // 5. 시험 마감 시간 검증
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndDate())) {
            throw new ExamDeadlineExceededException("시험 마감 시간이 지났습니다. 마감 시간: " + exam.getEndDate());
        }

        // 6. 개인별 시험 시간 초과 검증 (시작 시간 + 제한 시간)
        if (exam.getDurationMinute() != null) {
            LocalDateTime personalDeadline = examStudent.getStartedAt()
                    .plusMinutes(exam.getDurationMinute());
            if (now.isAfter(personalDeadline)) {
                throw new ExamDeadlineExceededException(
                        "시험 제한 시간이 초과되었습니다. 제한 시간: " + exam.getDurationMinute() + "분");
            }
        }

        // 검증 통과 - 제출 처리
        examStudent.submit();
    }
}