package com.edutrack.domain.assignment.repository;

import com.edutrack.domain.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLectureId(Long lectureId);

}