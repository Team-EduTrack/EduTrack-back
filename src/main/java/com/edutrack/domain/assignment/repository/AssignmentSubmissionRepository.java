package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

  // 특정 과제에 제출된 제출물 리스트
  List<AssignmentSubmission> findAllByAssignmentId(Long assignmentId);

  boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);

  Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(Long assignmentId,
      Long studentId);
}
