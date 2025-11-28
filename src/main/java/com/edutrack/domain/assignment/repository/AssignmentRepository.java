package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository <Assignment, Long> {


}
