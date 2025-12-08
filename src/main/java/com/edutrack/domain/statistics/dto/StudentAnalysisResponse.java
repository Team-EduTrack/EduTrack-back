package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentAnalysisResponse {
    // 평균 점수
    private final Double avgScore;

    // 취약 단원 리스트 (단원명)
    private final List<String> unitWeak;

    // 점수 추이 (시간순)
    private final List<Integer> trend;
}

