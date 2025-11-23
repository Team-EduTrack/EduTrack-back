package com.edutrack.api.student.repository;

import com.edutrack.domain.assignment.entity.Assignment;

import com.edutrack.api.student.dto.AssignmentSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 과제목록
public interface StudentAssignmentQueryRepository
        extends JpaRepository<Assignment, Long> {

    @Query(value = """
            SELECT 
                a.id                AS assignmentId,
                l.title             AS lectureTitle,
                a.title             AS title,
                a.start_date        AS startDate,
                a.end_date          AS endDate,
                s.score             AS score
            FROM lecture_student ls
            JOIN assignments a        ON a.lecture_id = ls.lecture_id
            JOIN lecture l            ON l.id = a.lecture_id
            LEFT JOIN assignment_submission s
                                       ON s.assignments_id = a.id
                                      AND s.user_id = :studentId
            WHERE ls.user_id = :studentId
            ORDER BY a.end_date DESC, a.id DESC
            """, nativeQuery = true)
    List<AssignmentSummaryResponse> findMyAssignments(@Param("studentId") Long studentId);
}