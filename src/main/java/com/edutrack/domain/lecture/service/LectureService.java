package com.edutrack.domain.lecture.service;


import com.edutrack.domain.lecture.dto.LectureDetailForTeacherResponse;
import com.edutrack.domain.lecture.dto.LectureForTeacherResponse;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.global.exception.LectureAccessDeniedException;
import com.edutrack.global.exception.LectureNotFoundException;
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

  /**
   * 해당 방식은 Batch 조회를 통해 N+1 문제를 방지하고,
   * 한 강사가 담당하는 강의 수가 적고 (많으면 대략 10~20개)
   * 강의당 학생 수가 수천 명 이하인 경우에 효율적이라고 생각합니다.
   * 위 두 조건이 지켜지지 않는 경우, DB 집계가 더 효율적이라고 생각합니다.
   */
  @Transactional(readOnly = true)
  public List<LectureForTeacherResponse> getLecturesByTeacherId(Long teacherId) {

    //강사의 강의 목록 조회
    List<Lecture> lectures = lectureRepository.findAllByTeacherId(teacherId);

    //강의 ID 목록 추출
    List<Long> lectureIds = lectures.stream()
        .map(Lecture::getId)
        .toList();

    // Batch 조회를 통한 해당 강의들의 모든 수강생 수 조회
    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureIdIn(lectureIds);

    // 강의별 수강생수 조회
    Map<Long, Long> studentCountMap = lectureStudents.stream()
        .collect(Collectors.groupingBy(ls -> ls.getLecture().getId(), Collectors.counting()));

    //각 강의와 수강생 수를 매핑하여 DTO 생성
    return lectures.stream()
        .map(lecture -> LectureForTeacherResponse.of(
            lecture,
            studentCountMap.getOrDefault(lecture.getId(), 0L).intValue()
        )).toList();
  }

  //강의 상세 조회 (선생용)
  @Transactional(readOnly = true)
  public LectureDetailForTeacherResponse getLectureDetailForTeacherId(Long lectureId, User user) {
    //강의 조회
    Lecture lecture = lectureRepository.findById(lectureId)
        .orElseThrow(() -> new LectureNotFoundException(lectureId));

    //권한 검증
    boolean isTeacherAndOwner = user.hasRole(RoleType.TEACHER)
        && lecture.getTeacher().getId().equals(user.getId());

    boolean isPrincipalOfAcademy = user.hasRole(RoleType.PRINCIPAL)
        && lecture.getAcademy().getId().equals(user.getAcademy().getId());

    if(!isTeacherAndOwner && !isPrincipalOfAcademy) {
      throw new LectureAccessDeniedException(lectureId, user.getId());
    }

    //강의에 배정된 수강생 리스트 조회
    List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);

    return new LectureDetailForTeacherResponse(
        lecture.getId(),
        lecture.getTitle(),
        lecture.getDescription(),
        lectureStudents
    );
  }

}
