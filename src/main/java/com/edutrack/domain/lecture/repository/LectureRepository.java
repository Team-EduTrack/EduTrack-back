package com.edutrack.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edutrack.domain.lecture.entity.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

  // teacherId가 일치하는 강의 목록 조회
  List<Lecture> findAllByTeacherId(Long teacherId);

  // academyId가 일치하는 강의 목록 조회 (원장용)
  List<Lecture> findAllByAcademyId(Long academyId);

}
