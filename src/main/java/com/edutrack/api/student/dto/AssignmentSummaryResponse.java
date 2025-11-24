package com.edutrack.api.student.dto;

import java.time.LocalDateTime;

public record AssignmentSummaryResponse(
        Long assignmentId,
        String lectureTitle,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer score
) {}
