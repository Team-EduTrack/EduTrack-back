package com.edutrack.api.student.repository;

import com.edutrack.api.student.dto.MyLectureResponse;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 학생 강의 조회 Repository
 * - 학생이 수강 중인 강의 목록 조회
 */
@Repository
public interface StudentLectureQueryRepository extends JpaRepository<LectureStudent, LectureStudentId> {

    /**
     * 학생의 수강 중인 강의 목록 조회 (JPQL + DTO Projection)
     * - 강의 시작일, 제목 순으로 정렬
     */
    @Query("""
            SELECT new com.edutrack.api.student.dto.MyLectureResponse(
                l.id,
                l.title,
                t.name,
                l.startDate,
                l.endDate
            )
            FROM LectureStudent ls
            JOIN ls.lecture l
            JOIN l.teacher t
            WHERE ls.student.id = :studentId
            ORDER BY l.startDate ASC, l.title ASC
            """)
    List<MyLectureResponse> findMyLectures(@Param("studentId") Long studentId);

    /**
     * 학생이 해당 강의를 수강 중인지 확인
     */
    boolean existsById(LectureStudentId id);
}
