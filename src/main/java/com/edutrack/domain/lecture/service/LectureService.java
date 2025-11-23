package com.edutrack.domain.lecture.service;

import com.edutrack.domain.lecture.dto.LectureForStudentResponseDto;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponseDto;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.lectureStudent.entity.LectureStudent;
import com.edutrack.domain.lecture.lectureStudent.repository.LectureStudentRepository;
import com.edutrack.domain.lecture.repository.LectureRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureService {

  private final LectureRepository lectureRepository;
  private final LectureStudentRepository lectureStudentRepository;


  //강사의 ID로 강의 목록과 각 강의의 수강생 수 조회
  @Transactional(readOnly = true)
  public List<LectureForTeacherResponseDto> getLecturesByTeacherId(Long teacherId) {

    //강사의 강의 목록 조회
    List<Lecture> lectures = lectureRepository.findAllByTeacherId(teacherId);

    //강의 ID 목록 추출
    List<Long> lectureIds = lectures.stream()
        .map(Lecture::getId)
        .toList();

    // Batch 조회를 통한 해당 강의들의 모든 수강생 수 조회
    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllStudentsByLectureIdIn(lectureIds);

    // 강의별 수강생수 조회
    Map<Long, Long> studentCountMap = lectureStudents.stream()
        .collect(Collectors.groupingBy(ls -> ls.getLecture().getId(), Collectors.counting()));

    //각 강의와 수강생 수를 매핑하여 DTO 생성
    return lectures.stream()
        .map(lecture -> LectureForTeacherResponseDto.of(
            lecture,
            studentCountMap.getOrDefault(lecture.getId(), 0L).intValue()
        )).toList();
  }

  //학생의 ID로 수강 중인 강의 목록 조회
  @Transactional(readOnly = true)
  public List<LectureForStudentResponseDto> getLecturesByStudentId(Long studentId) {
    List<LectureStudent> lectures = lectureStudentRepository.findAllLecturesByStudentId(studentId);

    return lectures.stream()
        .map(ls -> LectureForStudentResponseDto.of(ls.getLecture())).toList();
  }

}
