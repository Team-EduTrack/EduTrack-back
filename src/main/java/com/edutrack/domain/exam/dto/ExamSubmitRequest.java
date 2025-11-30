package com.edutrack.domain.exam.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExamSubmitRequest {

    @Getter
    @NoArgsConstructor
    public static class AnswerRequest {
        private Long questionId;
        private Integer submittedAnswerNumber;
    }

    private List<AnswerRequest> answers;
}
