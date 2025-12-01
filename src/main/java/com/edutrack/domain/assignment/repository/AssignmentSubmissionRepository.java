package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    boolean existsByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
    
    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);

}
