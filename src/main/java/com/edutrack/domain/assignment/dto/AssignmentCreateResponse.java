package com.edutrack.domain.assignment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentCreateResponse {

    private final Long assignmentId;
}