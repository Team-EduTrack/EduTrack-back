package com.edutrack.domain.statistics.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.exam.entity.Difficulty;
import com.edutrack.domain.statistics.dto.DifficultyStatisticsResponse;
import com.edutrack.domain.statistics.repository.ExamStatisticsRepository;

import lombok.RequiredArgsConstructor;

/**
 * 시험 통계 서비스
 * - 난이도별 정답률 등 통계 계산 로직 제공
 * - 여러 서비스에서 재사용 가능
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamDifficultyStatisticsService {

    private final ExamStatisticsRepository examStatisticsRepository;

    /**
     * 특정 시험의 난이도별 정답률 통계 계산
     */
    public Map<Difficulty, Double> getDifficultyCorrectRateMapForExam(Long examId) {
        List<DifficultyStatisticsResponse> results = examStatisticsRepository.findDifficultyStatisticsByExamId(examId);
        return convertToDifficultyRateMap(results);
    }

    /**
     * 특정 학생의 특정 시험에 대한 난이도별 정답률 통계 계산
     */
    public Map<Difficulty, Double> getDifficultyCorrectRateMapForStudentExam(
            Long examId, 
            Long studentId
    ) {
        List<DifficultyStatisticsResponse> results = examStatisticsRepository
                .findDifficultyStatisticsByExamIdAndStudentId(examId, studentId);
        return convertToDifficultyRateMap(results);
    }

    /**
     * 강의의 모든 시험에 대한 난이도별 정답률 통계 계산
     * 
     * @param lectureId 강의 ID
     * @return 난이도별 정답률 Map
     */
    public Map<Difficulty, Double> getDifficultyCorrectRateMapForLecture(Long lectureId) {
        List<DifficultyStatisticsResponse> results = examStatisticsRepository.findDifficultyStatisticsByLectureId(lectureId);
        return convertToDifficultyRateMap(results);
    }

    /**
     * 쿼리 결과를 난이도별 정답률 Map으로 변환
     */
    private Map<Difficulty, Double> convertToDifficultyRateMap(List<DifficultyStatisticsResponse> results) {
        Map<Difficulty, Double> difficultyRateMap = new HashMap<>();
        
        for (DifficultyStatisticsResponse result : results) {
            Difficulty difficulty = result.getDifficulty();
            Long totalQuestions = result.getTotalQuestions();
            Long correctQuestions = result.getCorrectQuestions();
            
            // 정답률 계산 (전체 문제가 0개면 0.0)
            double correctRate = totalQuestions > 0 
                    ? (double) correctQuestions / totalQuestions 
                    : 0.0;
            
            difficultyRateMap.put(difficulty, correctRate);
        }
        
        // 모든 난이도가 포함되도록 기본값 설정 (없으면 0.0)
        for (Difficulty difficulty : Difficulty.values()) {
            difficultyRateMap.putIfAbsent(difficulty, 0.0);
        }
        
        return difficultyRateMap;
    }
}
