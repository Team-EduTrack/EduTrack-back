package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

  // 학생이 이미 이 과제를 제출했는지 확인
  boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);


}
