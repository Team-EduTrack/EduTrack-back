package com.edutrack.api.student.repository;

import com.edutrack.api.student.dto.ExamSummaryResponse;
import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStatus;
import com.edutrack.domain.exam.entity.ExamStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 학생 시험 조회 Repository
 * - 학생이 수강 중인 강의의 시험 목록 조회
 */
@Repository
public interface StudentExamQueryRepository extends JpaRepository<ExamStudent, ExamStudentId> {

    /**
     * 학생의 시험 목록 조회 (JPQL + DTO Projection)
     * - 수강 중인 강의의 시험만 조회
     * - 응시 상태와 획득 점수 포함
     * - PUBLISHED, CLOSED 상태의 시험만 조회 (학생에게 공개된 시험)
     * - 시작일 기준 내림차순 정렬
     */
    @Query("""
            SELECT new com.edutrack.api.student.dto.ExamSummaryResponse(
                e.id,
                l.title,
                e.title,
                e.startDate,
                e.endDate,
                es.earnedScore,
                CAST(es.status AS string)
            )
            FROM LectureStudent ls
            JOIN ls.lecture l
            JOIN Exam e ON e.lecture.id = l.id
            LEFT JOIN ExamStudent es ON es.exam.id = e.id AND es.student.id = :studentId
            WHERE ls.student.id = :studentId
              AND e.status IN :statuses
            ORDER BY e.startDate DESC, e.id DESC
            """)
    List<ExamSummaryResponse> findMyExams(@Param("studentId") Long studentId,
    @Param("statuses") List<ExamStatus> statuses);
}
