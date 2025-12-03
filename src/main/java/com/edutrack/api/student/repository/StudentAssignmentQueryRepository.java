package com.edutrack.api.student.repository;

import com.edutrack.api.student.dto.AssignmentSummaryResponse;
import com.edutrack.domain.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 학생 과제 조회 Repository
 * - 학생이 수강 중인 강의의 과제 목록 조회
 */
@Repository
public interface StudentAssignmentQueryRepository extends JpaRepository<Assignment, Long> {

    /**
     * 학생의 과제 목록 조회 (JPQL + DTO Projection)
     * - 수강 중인 강의의 과제만 조회
     * - 제출 여부와 점수 포함
     * - 마감일 기준 내림차순 정렬
     */
    @Query("""
            SELECT new com.edutrack.api.student.dto.AssignmentSummaryResponse(
                a.id,
                l.title,
                a.title,
                a.startDate,
                a.endDate,
                s.score
            )
            FROM LectureStudent ls
            JOIN ls.lecture l
            JOIN Assignment a ON a.lecture.id = l.id
            LEFT JOIN AssignmentSubmission s ON s.assignment.id = a.id AND s.student.id = :studentId
            WHERE ls.student.id = :studentId
            ORDER BY a.endDate DESC, a.id DESC
            """)
    List<AssignmentSummaryResponse> findMyAssignments(@Param("studentId") Long studentId);
}
