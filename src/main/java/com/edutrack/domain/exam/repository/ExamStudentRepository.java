package com.edutrack.domain.exam.repository;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 시험 응시 기록 Repository
 */
@Repository
public interface ExamStudentRepository extends JpaRepository<ExamStudent, ExamStudentId> {

    /**
     * 시험 ID와 학생 ID로 응시 기록 조회
     */
    @Query("SELECT es FROM ExamStudent es WHERE es.exam.id = :examId AND es.student.id = :studentId")
    Optional<ExamStudent> findByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);

    /**
     * 학생의 모든 시험 응시 기록 조회
     */
    @Query("SELECT es FROM ExamStudent es " +
           "JOIN FETCH es.exam e " +
           "JOIN FETCH e.lecture l " +
           "WHERE es.student.id = :studentId " +
           "ORDER BY es.startedAt DESC")
    List<ExamStudent> findAllByStudentIdWithExam(@Param("studentId") Long studentId);

    /**
     * 시험 ID와 학생 ID로 응시 기록 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END " +
           "FROM ExamStudent es WHERE es.exam.id = :examId AND es.student.id = :studentId")
    boolean existsByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
}