package com.edutrack.domain.assignment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignmentSubmissionStudentViewResponse {

    // 제출 여부
    private final boolean submitted;

    // 과제 메타(항상 포함)
    private final Long assignmentId;
    private final String lectureName;              // 강의명
    private final String teacherName;              // 강사 이름
    private final String assignmentTitle;
    private final String assignmentDescription;
    private final LocalDateTime endDate;           // 과제 마감일

    // 제출 정보(제출 시)
    private final Long submissionId;
    private final String filePath;                 // 제출 파일 경로
    private final Integer score;                   // 점수 (null이면 미채점)
    private final String feedback;                 // 피드백
}