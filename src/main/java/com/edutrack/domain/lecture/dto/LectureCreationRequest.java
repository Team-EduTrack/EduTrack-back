package com.edutrack.domain.lecture.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class LectureCreationRequest {
    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "강사 ID는 필수입니다.")
    private Long teacherId;

    @NotEmpty(message = "강의 요일은 최소 1개 이상 선택해야 합니다.")
    private List<DayOfWeek> daysOfWeek;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @Future(message = "종료일은 현재 시점 이후여야 합니다.")
    private LocalDateTime endDate;
}
