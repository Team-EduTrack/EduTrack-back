package com.edutrack.domain.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentId;

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
     * 학생의 특정 강의에 대한 모든 시험 응시 기록 조회
     * @param studentId 학생 ID
     * @param lectureId 강의 ID
     * @return 시험 응시 기록 목록 (제출일 기준 오름차순 정렬)
     */
    @Query("SELECT es FROM ExamStudent es " +
           "JOIN FETCH es.exam e " +
           "JOIN FETCH e.lecture l " +
           "WHERE es.student.id = :studentId " +
           "AND e.lecture.id = :lectureId " +
           "ORDER BY es.submittedAt ASC")
    List<ExamStudent> findByStudentIdAndLectureId(
            @Param("studentId") Long studentId,
            @Param("lectureId") Long lectureId
    );

    /**
     * 시험 ID와 학생 ID로 응시 기록 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END " +
           "FROM ExamStudent es WHERE es.exam.id = :examId AND es.student.id = :studentId")
    boolean existsByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);

    List<ExamStudent> findByExamId(Long examId);


  /**
   * 특정 시험 목록과 학생 목록에 대한 응시 기록을 한 번에 조회
   * @param examIds 시험 ID 목록
   * @param studentIds 학생 ID 목록
   * @return 응시 기록 목록
   */
  @Query("""
    SELECT es 
    FROM ExamStudent es
    WHERE es.exam.id IN :examIds 
      AND es.student.id IN :studentIds
    """)
  List<ExamStudent> findAllByExamIdsAndStudentIds(
      @Param("examIds") List<Long> examIds,
      @Param("studentIds") List<Long> studentIds
  );

  /**
   * 특정 시험 ID 목록에 대한 모든 응시 기록을 한 번에 조회
   * @param examIds 시험 ID 목록
   * @return 응시 기록 목록
   */
  @Query("""
        SELECT es 
        FROM ExamStudent es
        WHERE es.exam.id IN :examIds
        """)
  List<ExamStudent> findAllByExamIds(@Param("examIds") List<Long> examIds);
}