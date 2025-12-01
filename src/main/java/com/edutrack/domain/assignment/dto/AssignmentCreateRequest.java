package com.edutrack.domain.assignment.dto;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AssignmentCreateRequest {

    private Long lectureId;

    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}