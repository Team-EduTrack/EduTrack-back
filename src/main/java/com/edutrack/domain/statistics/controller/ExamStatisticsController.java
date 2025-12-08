package com.edutrack.domain.statistics.controller;

import com.edutrack.domain.statistics.dto.ExamDistributionResponse;
import com.edutrack.domain.statistics.service.ExamStatisticsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamStatisticsController {

    private final ExamStatisticsService examStatisticsService;

    @GetMapping("/{examId}/distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ExamDistributionResponse> getScoreDistribution(
            @PathVariable Long examId,
            Authentication authentication) {

        ExamDistributionResponse response = examStatisticsService.getScoreDistribution(examId);

        return ResponseEntity.ok(response);
    }
}
