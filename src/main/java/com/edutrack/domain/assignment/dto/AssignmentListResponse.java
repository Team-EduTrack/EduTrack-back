package com.edutrack.domain.assignment.dto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignmentListResponse {

    private final Long assignmentId;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
}