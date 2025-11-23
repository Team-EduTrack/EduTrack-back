package com.edutrack.domain.lecture.lectureStudent.repository;

import com.edutrack.domain.lecture.lectureStudent.entity.LectureStudent;
import com.edutrack.domain.lecture.lectureStudent.entity.LectureStudentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureStudentRepository extends JpaRepository<LectureStudent, LectureStudentId> {


  //특정 학생이 수강 중인 강의 목록
  List<LectureStudent> findAllLecturesByStudentId(Long studentId);

  // Batch 조회 : 여러 강의 ID에 대한 각 강의 별 수강생 수 조회
  List<LectureStudent> findAllStudentsByLectureIdIn(List<Long> lectureIds);


}
