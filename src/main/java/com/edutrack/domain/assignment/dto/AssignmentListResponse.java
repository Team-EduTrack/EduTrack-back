package com.edutrack.domain.assignment.dto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignmentListResponse {

    private final Long assignmentId;
    private final String title;
    private final LocalDateTime endDate;              // 마감일
    private final AssignmentSubmissionStatus status;  // 미제출 / 제출완료
}