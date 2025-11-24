package com.edutrack.api.student.dto;
import java.time.LocalDateTime;


public record ExamSummaryResponse(
        Long examId,
        String lectureTitle,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer earnedScore,
        String status
) {}
