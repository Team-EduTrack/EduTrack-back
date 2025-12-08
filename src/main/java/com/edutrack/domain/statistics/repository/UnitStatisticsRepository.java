package com.edutrack.domain.statistics.repository;

import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse;
import com.edutrack.domain.statistics.dto.UnitCorrectRateResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UnitStatisticsRepository extends JpaRepository<ExamStudentAnswer, Long> {

    // 1) 전체 학생 기준 단원 정답률
    @Query("""
        SELECT new com.edutrack.domain.statistics.dto.UnitCorrectRateResponse(
            a.unitId,
            COUNT(a),
            SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
            CASE 
                WHEN COUNT(a) = 0 THEN 0.0
                ELSE (SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END) * 100.0 / COUNT(a))
            END
        )
        FROM ExamStudentAnswer a
        WHERE a.unitId = :unitId
        GROUP BY a.unitId
        """)
    Optional<UnitCorrectRateResponse> findUnitCorrectRate(@Param("unitId") Long unitId);


    // 2) 특정 학생 기준 단원 정답률
    @Query("""
            SELECT new com.edutrack.domain.statistics.dto.StudentUnitCorrectRateResponse(
                a.unitId,
                a.examStudent.student.id,
                COUNT(a),
                SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
                CASE 
                    WHEN COUNT(a) = 0 THEN 0.0
                    ELSE (SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END) * 100.0 / COUNT(a))
                END
            )
            FROM ExamStudentAnswer a
            WHERE a.unitId = :unitId
              AND a.examStudent.student.id = :studentId
            GROUP BY a.unitId, a.examStudent.student.id
            """)
    Optional<StudentUnitCorrectRateResponse> findStudentUnitCorrectRate(
            @Param("studentId") Long studentId,
            @Param("unitId") Long unitId
    );
}