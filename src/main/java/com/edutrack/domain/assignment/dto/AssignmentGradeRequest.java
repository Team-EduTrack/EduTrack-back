package com.edutrack.domain.assignment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AssignmentGradeRequest {

    @NotNull(message = "점수는 필수입니다.")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다.")
    @Max(value = 100, message = "점수는 100점을 초과할 수 없습니다.")
    private Integer score;

    private String feedback;
}