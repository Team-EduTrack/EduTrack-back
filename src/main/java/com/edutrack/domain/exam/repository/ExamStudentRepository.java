package com.edutrack.domain.exam.repository;

import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamStudentRepository extends JpaRepository<ExamStudent, ExamStudentId> {

    Optional<ExamStudent> findByExamIdAndStudentId(Long examId, Long studentId);
}