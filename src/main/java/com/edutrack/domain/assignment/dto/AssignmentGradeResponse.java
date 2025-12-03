package com.edutrack.domain.assignment.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentGradeResponse {

    private final Long submissionId;
    private final Long assignmentId;
    private final Long studentId;
    private final Integer score;
    private final String feedback;
}