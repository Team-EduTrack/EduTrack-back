package com.edutrack.domain.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

  // 특정 과제에 제출된 제출물 리스트
  List<AssignmentSubmission> findAllByAssignmentId(Long assignmentId);

  boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);

  Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(Long assignmentId,
      Long studentId);

  // 여러 과제와 여러 학생에 대한 제출물 조회
  @Query("""
    SELECT s 
    FROM AssignmentSubmission s
    WHERE s.assignment.id IN :assignmentIds 
      AND s.student.id IN :studentIds
    """)
  List<AssignmentSubmission> findAllByAssignmentIdsAndStudentIds(
      @Param("assignmentIds") List<Long> assignmentIds,
      @Param("studentIds") List<Long> studentIds
  );
}
