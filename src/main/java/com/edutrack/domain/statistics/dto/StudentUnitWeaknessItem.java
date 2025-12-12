package com.edutrack.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentUnitWeaknessItem {
    private final Long unitId;
    private final String unitName;
    private final Double correctRate;
    private final long totalQuestions;
}
