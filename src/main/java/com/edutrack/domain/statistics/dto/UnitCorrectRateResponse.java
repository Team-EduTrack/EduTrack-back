package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnitCorrectRateResponse {
    private final Long unitId;
    private final long totalTryCount;
    private final long correctCount;
    private final double correctRate;
}
