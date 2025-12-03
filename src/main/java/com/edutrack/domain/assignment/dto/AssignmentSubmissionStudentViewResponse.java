package com.edutrack.domain.assignment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentSubmissionStudentViewResponse {

    private final Long submissionId;
    private final Long assignmentId;

    private final String lectureName;   // 강의명
    private final String teacherName;   // 강사 이름

    private final String studentLoginId;  // 본인 아이디
    private final String studentName;     // 본인 이름

    private final String assignmentTitle;
    private final String assignmentDescription;

    private final String filePath;  // 제출 파일 경로
    private final Integer score;    // 점수 (null이면 미채점)
    private final String feedback;  // 피드백
}