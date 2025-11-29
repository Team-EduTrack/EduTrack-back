package com.edutrack.domain.exam.dto;

import com.edutrack.domain.exam.entity.Choice;
import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ExamDetailResponse {

    private final Long examId;
    private final Long lectureId;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer durationMinute;
    private final String status;

    private final List<QuestionDetail> questions;


    @Getter
    @Builder
    public static class QuestionDetail {
        private final Long questionId;
        private final String content;
        private final Integer score;
        private final Integer answerNumber;
        private final String difficulty;
        private final Long unitId;
        private final List<ChoiceDetail> choices;


        public static QuestionDetail fromEntity(Question question) {
            return QuestionDetail.builder()
                    .questionId(question.getId())
                    .content(question.getContent())
                    .score(question.getScore())
                    .answerNumber(question.getAnswerNumber())
                    .difficulty(question.getDifficulty().name())
                    .unitId(question.getUnitId())
                    .choices(question.getChoices().stream()
                            .map(ChoiceDetail::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }
    }


    @Getter
    @Builder
    public static class ChoiceDetail {
        private final Long choiceId;
        private final String content;
        private final Integer choiceNumber;


        public static ChoiceDetail fromEntity(Choice choice) {
            return ChoiceDetail.builder()
                    .choiceId(choice.getId())
                    .content(choice.getContent())
                    .choiceNumber(choice.getChoiceNumber())
                    .build();
        }
    }


    public static ExamDetailResponse of(Exam exam, List<Question> questions) {
        return ExamDetailResponse.builder()
                .examId(exam.getId())
                .lectureId(exam.getLecture().getId())
                .title(exam.getTitle())
                .startDate(exam.getStartDate())
                .endDate(exam.getEndDate())
                .durationMinute(exam.getDurationMinute())
                .status(exam.getStatus().name())
                .questions(questions.stream()
                        .map(QuestionDetail::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}