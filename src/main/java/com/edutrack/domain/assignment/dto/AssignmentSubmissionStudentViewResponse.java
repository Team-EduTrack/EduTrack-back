package com.edutrack.domain.assignment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentSubmissionStudentViewResponse {

    private final Long submissionId;
    private final Long assignmentId;

    private final String assignmentTitle;
    private final String assignmentDescription;

    private final String filePath;
    private final Integer score;
    private final String feedback;
}