package com.edutrack.domain.unit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UnitCreateRequest {

    @NotNull(message = "강의 ID는 필수입니다.")
    private Long lectureId;

    @NotBlank(message = "단원명은 필수입니다.")
    private String name;
}

