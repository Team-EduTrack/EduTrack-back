package com.edutrack.domain.statistics.service;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.repository.ExamRepository;
import com.edutrack.domain.exam.repository.ExamStudentRepository;
import com.edutrack.domain.statistics.dto.ExamDistributionResponse;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamStatisticsService {

    private final ExamRepository examRepository;
    private final ExamStudentRepository examStudentRepository;

    private static final int SCORE_INTERVAL = 10;
    private static final List<String> DEFINED_RANGES = Arrays.asList(
            "0-10", "10-20", "20-30", "30-40", "40-50",
            "50-60", "60-70", "70-80", "80-90", "90-100"
    );


    @Transactional(readOnly = true)
    public ExamDistributionResponse getScoreDistribution(Long examId) {

        if (!examRepository.existsById(examId)) {
            throw new NotFoundException("시험을 찾을수없습니다. ID: " + examId);
        }

        List<ExamStudent> submissions = examStudentRepository.findByExamId(examId);

        List<Integer> scores = submissions.stream()
                .map(ExamStudent::getEarnedScore)
                .filter(score -> score != null)
                .collect(Collectors.toList());

        int[] counts = new int[10];

        for (int score : scores) {
            int index = Math.min(score / SCORE_INTERVAL, 9);
            counts[index]++;
        }


        return ExamDistributionResponse.builder()
                .ranges(DEFINED_RANGES)
                .counts(Arrays.stream(counts).boxed().collect(Collectors.toList()))
                .totalSubmissions(scores.size())
                .build();
    }
}