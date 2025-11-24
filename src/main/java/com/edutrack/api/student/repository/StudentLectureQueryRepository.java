package com.edutrack.api.student.repository;

import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.api.student.dto.MyLectureResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// 강의 조회
public interface StudentLectureQueryRepository
        extends JpaRepository<LectureStudent, LectureStudentId> {

    @Query(value = """
            SELECT 
                l.id            AS lectureId,
                l.title         AS lectureTitle,
                t.name          AS teacherName,
                l.start_date    AS startDate,
                l.end_date      AS endDate
            FROM lecture_student ls
            JOIN lecture l      ON ls.lecture_id = l.id
            JOIN users t         ON l.teacher_id = t.id
            WHERE ls.user_id = :studentId
            ORDER BY l.start_date, l.title
            """, nativeQuery = true)
    List<MyLectureResponse> findMyLectures(@Param("studentId") Long studentId);
}

