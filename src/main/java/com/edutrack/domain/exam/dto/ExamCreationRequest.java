package com.edutrack.domain.exam.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExamCreationRequest {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long lectureId;

    @NotBlank(message = "시험 제목은 필수입니다.")
    private String title;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @Future(message = "종료일은 현재 시점 이후여야 합니다.")
    private LocalDateTime endDate;

    @NotNull(message = "응시 시간은 필수입니다.")
    @Min(value = 5, message = "응시 시간은 최소 5분 이상이어야 합니다.")
    private Integer durationMinute;
}
