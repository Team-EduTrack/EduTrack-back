package com.edutrack.domain.assignment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AssignmentCreateRequest {

    private String title;

    private String description;

    // 프론트에서 보내는 dueDate
    @NotNull
    @JsonProperty("dueDate")
    private LocalDateTime endDate;

    /**
     * startDate는 백엔드에서 자동 설정
     */
    public LocalDateTime getStartDate() {
        return LocalDateTime.now();
    }
}