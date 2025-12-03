package com.edutrack.domain.exam.service;

import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.exam.dto.ExamCreationRequest;
import com.edutrack.domain.exam.dto.ExamDetailResponse;
import com.edutrack.domain.exam.dto.QuestionRegistrationRequest;
import com.edutrack.domain.exam.entity.Choice;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.exam.entity.Question;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.QuestionRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
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
}