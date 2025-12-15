package com.edutrack.domain.exam.repository;

import com.edutrack.domain.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 시험 문제 Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 시험 ID로 문제 목록 조회
     */
    List<Question> findByExamId(Long examId);

    /**
     * 시험 ID로 문제 목록 조회 (보기 포함)
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.choices " +
           "WHERE q.exam.id = :examId " +
           "ORDER BY q.id")
    List<Question> findByExamIdWithChoices(@Param("examId") Long examId);

    /**
     * 시험의 총 배점 합계 조회
     */
    @Query("SELECT COALESCE(SUM(q.score), 0) FROM Question q WHERE q.exam.id = :examId")
    int sumScoreByExamId(@Param("examId") Long examId);

    /**
     * 시험의 문제 수 조회
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.exam.id = :examId")
    int countByExamId(@Param("examId") Long examId);

    // 강의 전체 문항 조회 (시험 여러 개 포함)
    List<Question> findAllByExam_Lecture_Id(Long lectureId);
}
