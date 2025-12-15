package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentUnitCorrectRateResponse {

    private final Long unitId;
    private final Long studentId;
    private final long totalTryCount;
    private final long correctCount;
    private final double correctRate;
}
