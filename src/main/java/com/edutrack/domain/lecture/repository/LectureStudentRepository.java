package com.edutrack.domain.lecture.repository;

import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureStudentRepository extends JpaRepository<LectureStudent, LectureStudentId> {

  // Batch 조회를 통한 여러 강의 ID에 대한 각 강의별 수강생 수 조회
  List<LectureStudent> findAllByLectureIdIn(List<Long> lectureIds);

  // 특정 강의에 수강 중인 모든 학생 조회
  List<LectureStudent> findAllByLectureId(Long lectureId);
}
