package com.edutrack.domain.exam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/*
 *  학생이 시험 응시하면 반환되는 응답 DTO
 * */


@Getter
@Builder
public class ExamStartResponse {
    private Long examId;
    private Long studentId;
    private LocalDateTime startedAt;
    private String status;
}
