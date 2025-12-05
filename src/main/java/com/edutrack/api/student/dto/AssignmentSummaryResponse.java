package com.edutrack.api.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 과제 목록 응답 DTO
 * - JPQL DTO Projection용 클래스
 */
@Getter
@Builder
public class AssignmentSummaryResponse {
    
    private final Long assignmentId;
    private final String lectureTitle;
    private final String title;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer score;

    /**
     * JPQL new 연산자용 생성자
     */
    public AssignmentSummaryResponse(Long assignmentId, String lectureTitle,
                                     String title, LocalDateTime startDate,
                                     LocalDateTime endDate, Integer score) {
        this.assignmentId = assignmentId;
        this.lectureTitle = lectureTitle;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.score = score;
    }
}
