package com.edutrack.domain.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutrack.domain.exam.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByLectureId(Long lectureId);
    
    // 여러 강의 ID에 대한 시험 목록 배치 조회
    List<Exam> findByLectureIdIn(List<Long> lectureIds);
}
