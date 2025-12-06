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

    @Transactional(readOnly = true)
    public ExamDistributionResponse getScoreDistribution(Long examId) {
        //시험존재확인
        if (!examRepository.existsById(examId)) {
            throw new NotFoundException("시험을 찾을수없습니다. ID: " + examId);
        }

        //해당 시험의 모든 응시학생 점수 조회
        List<ExamStudent> submissions = examStudentRepository.findAll();

        List<Integer> scores = submissions.stream()
                .filter(es -> es.getExam().getId().equals(examId) && es.getEarnedScore() != null)
                .map(ExamStudent::getEarnedScore)
                .collect(Collectors.toList());

        //점수 구간별 집계 히스토그램 데이터생성
        int[] counts = new int[10];

        for (int score : scores) {
            int index = Math.min(score / SCORE_INTERVAL, 9);
            counts[index]++;

        }

        List<String> ranges = Arrays.stream(counts)
                .mapToObj((count) -> {
                    int start = count * SCORE_INTERVAL;
                    int end = start + SCORE_INTERVAL;
                    return start + "-" + end;
                })
                .limit(10)
                .collect(Collectors.toList());

        List<String> definedRanges = Arrays.asList("0-10", "10-20", "20-30", "30-40", "40-50", "50-60", "60-70", "70-80", "80-90", "90-100");

        return ExamDistributionResponse.builder()
                .ranges(definedRanges)
                .counts(Arrays.stream(counts).boxed().collect(Collectors.toList()))
                .totalSubmissions(submissions.size())
                .build();

    }

}
