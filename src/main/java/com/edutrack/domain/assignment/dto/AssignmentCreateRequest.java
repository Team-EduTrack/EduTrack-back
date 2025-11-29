package com.edutrack.domain.assignment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AssignmentCreateRequest {

    private Long lectureId;

    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}