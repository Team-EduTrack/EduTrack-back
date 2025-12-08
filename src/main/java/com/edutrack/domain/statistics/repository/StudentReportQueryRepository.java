package com.edutrack.domain.statistics.repository;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.statistics.dto.StudentExamSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentReportQueryRepository extends JpaRepository<ExamStudent, Long> {

    @Query("""
            SELECT new com.edutrack.domain.statistics.dto.StudentExamSummaryResponse(
                e.id,
                e.title,
                l.title,
                e.totalScore,
                es.earnedScore,
                es.submittedAt
            )
            FROM ExamStudent es
            JOIN es.exam e
            JOIN e.lecture l
            WHERE es.student.id = :studentId
              AND es.status = com.edutrack.domain.exam.entity.StudentExamStatus.GRADED
            ORDER BY es.submittedAt DESC
            """)
    List<StudentExamSummaryResponse> findExamSummaryByStudentId(@Param("studentId") Long studentId);
}