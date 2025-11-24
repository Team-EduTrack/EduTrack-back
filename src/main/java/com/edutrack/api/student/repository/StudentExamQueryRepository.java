package com.edutrack.api.student.repository;

import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentId;
import com.edutrack.api.student.dto.ExamSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 시험 목록
public interface StudentExamQueryRepository
        extends JpaRepository<ExamStudent, ExamStudentId> {

    @Query(value = """
            SELECT 
                e.id               AS examId,
                l.title            AS lectureTitle,
                e.title            AS title,
                e.start_date       AS startDate,
                e.end_date         AS endDate,
                es.earned_score    AS earnedScore,
                es.status          AS status
            FROM lecture_student ls
            JOIN exam e            ON e.lecture_id = ls.lecture_id
            JOIN lecture l         ON l.id = e.lecture_id
            LEFT JOIN exam_student es
                                     ON es.exam_id = e.id
                                    AND es.user_id = :studentId
            WHERE ls.user_id = :studentId
            ORDER BY e.start_date DESC, e.id DESC
            """, nativeQuery = true)
    List<ExamSummaryResponse> findMyExams(@Param("studentId") Long studentId);
}