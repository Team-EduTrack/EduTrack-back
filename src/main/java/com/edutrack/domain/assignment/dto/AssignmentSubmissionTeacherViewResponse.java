package com.edutrack.domain.assignment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentSubmissionTeacherViewResponse {

    private final Long submissionId;
    private final Long assignmentId;

    private final String lectureName;      // 강의 명
    private final String teacherName;      // 강사 이름

    private final String studentLoginId;   // 학생 아이디
    private final String studentName;      // 학생 이름

    private final String assignmentTitle;       // 과제 제목
    private final String assignmentDescription; // 과제 설명

    private final String filePath;         // 제출 파일 경로

    private final Integer score;           // 점수
    private final String feedback;         // 피드백
}