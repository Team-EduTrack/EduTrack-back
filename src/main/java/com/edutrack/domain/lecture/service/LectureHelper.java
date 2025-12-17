package com.edutrack.domain.lecture.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.LectureAccessDeniedException;
import com.edutrack.global.exception.LectureNotFoundException;
import com.edutrack.global.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureHelper {

  private final LectureRepository lectureRepository;
  private final UserRepository userRepository;


  /**
   * 강의를 조회하고 권한을 검증한 후 반환
   * 조회 → 조회 → 검증 패턴을 한 번에 처리하여 코드 중복을 제거하고 검증 누락을 방지
   * 
   * 권한: 강의를 담당하는 강사(TEACHER) 또는 해당 학원의 원장(PRINCIPAL)만 접근 가능
   * 
   * @param lectureId 강의 ID
   * @param userId 사용자 ID (강사 또는 원장)
   * @return 권한이 검증된 Lecture
   * @throws LectureNotFoundException 강의를 찾을 수 없는 경우
   * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
   * @throws LectureAccessDeniedException 권한이 없는 경우
   */
  public Lecture getLectureWithValidation(Long lectureId, Long userId) {
    Lecture lecture = getLectureOrThrow(lectureId);
    User user = getUserOrThrow(userId);
    validateLectureAcess(lectureId, user, lecture);
    return lecture;
  }

  public Lecture getLectureOrThrow(Long lectureId) {
    Lecture lecture = lectureRepository.findById(lectureId)
        .orElseThrow(() -> new LectureNotFoundException(lectureId));
    return lecture;
  }

  public User getUserOrThrow(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID=" + userId));
    return user;
  }

  public Set<User> getValidStudents(List<Long> studentIds, Lecture lecture) {
    return userRepository.findAllById(studentIds).stream()
        .filter(s -> s.getAcademy().getId().equals(lecture.getAcademy().getId()))
        .filter(this::isStudent)
        .collect(Collectors.toSet());
  }


  public boolean isStudent(User user) {
    return user.getUserToRoles().stream()
        .anyMatch(ur -> ur.getRole().getName().equals(RoleType.STUDENT));
  }

  /**
   * 강의 접근 권한 검증
   * - 강사(TEACHER): 해당 강의를 담당하는 강사만 접근 가능
   * - 원장(PRINCIPAL): 해당 강의가 속한 학원의 원장만 접근 가능
   * 
   * @param lectureId 강의 ID
   * @param user 접근을 시도하는 사용자
   * @param lecture 강의 엔티티
   * @throws LectureAccessDeniedException 권한이 없는 경우
   */
  public void validateLectureAcess(Long lectureId, User user, Lecture lecture) {
    // 강사인 경우: 해당 강의를 담당하는 강사인지 확인
    boolean isTeacherAndOwner = user.hasRole(RoleType.TEACHER)
        && lecture.getTeacher().getId().equals(user.getId());

    // 원장인 경우: 해당 강의가 속한 학원의 원장인지 확인
    boolean isPrincipalOfAcademy = user.hasRole(RoleType.PRINCIPAL)
        && lecture.getAcademy().getId().equals(user.getAcademy().getId());

    if(!isTeacherAndOwner && !isPrincipalOfAcademy) {
      throw new LectureAccessDeniedException(lectureId, user.getId());
    }
  }
}
