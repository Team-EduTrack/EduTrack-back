package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
}
