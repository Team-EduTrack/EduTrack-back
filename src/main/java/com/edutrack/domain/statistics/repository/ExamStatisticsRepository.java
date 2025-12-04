package com.edutrack.domain.statistics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edutrack.domain.exam.entity.ExamStudentAnswer;

@Repository
public interface ExamStatisticsRepository extends JpaRepository<ExamStudentAnswer, Long> {

    //학생 개인의 특정 시험 난이도 별 통계
    @Query("SELECT esa.difficulty, COUNT(esa), SUM(CASE WHEN esa.correct = true THEN 1 ELSE 0 END) " +
    "FROM ExamStudentAnswer esa " +
    "WHERE esa.examStudent.exam.id = :examId " +
    "AND esa.examStudent.student.id = :studentId " +
    "GROUP BY esa.difficulty")
    List<Object[]> findDifficultyStatisticsByExamIdAndStudentId(
    @Param("examId") Long examId,
     @Param("studentId") Long studentId
    );

    //특정 시험에 대한 난이도 별 통계
    @Query("""
            SELECT 
                esa.difficulty,
                COUNT(esa) as totalQuestions,
                SUM(CASE WHEN esa.correct = true THEN 1 ELSE 0 END) as correctQuestions
            FROM ExamStudentAnswer esa
            WHERE esa.examStudent.exam.id = :examId
            GROUP BY esa.difficulty
            """)
    List<Object[]> findDifficultyStatisticsByExamId(@Param("examId") Long examId);

    //강의의 모든 시험에 대한 난이도별 통계
    @Query("""
            SELECT 
                esa.difficulty,
                COUNT(esa) as totalQuestions,
                SUM(CASE WHEN esa.correct = true THEN 1 ELSE 0 END) as correctQuestions
            FROM ExamStudentAnswer esa
            WHERE esa.examStudent.exam.lecture.id = :lectureId
            GROUP BY esa.difficulty
            """)
    List<Object[]> findDifficultyStatisticsByLectureId(@Param("lectureId") Long lectureId);


}
