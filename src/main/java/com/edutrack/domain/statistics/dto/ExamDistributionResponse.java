package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExamDistributionResponse {
    //점수구간
    private final List<String> ranges;

    private final List<Integer> counts;

    private final int totalSubmissions;

}
