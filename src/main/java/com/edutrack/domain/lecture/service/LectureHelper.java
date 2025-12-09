package com.edutrack.domain.lecture.service;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.LectureAccessDeniedException;
import com.edutrack.global.exception.LectureNotFoundException;
import com.edutrack.global.exception.UserNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureHelper {

  private final LectureRepository lectureRepository;
  private final UserRepository userRepository;

  public Lecture getLectureOrThrow(Long lectureId) {
    Lecture lecture = lectureRepository.findById(lectureId)
        .orElseThrow(() -> new LectureNotFoundException(lectureId));
    return lecture;
  }

  public User getTeacherOrThrow(Long teacherId) {
    User teacher = userRepository.findById(teacherId)
        .orElseThrow(() -> new UserNotFoundException("선생님을 찾을 수 없습니다. ID=" + teacherId));
    return teacher;
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

  public static void validateLectureAcess(Long lectureId, User teacher, Lecture lecture) {
    boolean isTeacherAndOwner = teacher.hasRole(RoleType.TEACHER)
        && lecture.getTeacher().getId().equals(teacher.getId());

    boolean isPrincipalOfAcademy = teacher.hasRole(RoleType.PRINCIPAL)
        && lecture.getAcademy().getId().equals(teacher.getAcademy().getId());

    if(!isTeacherAndOwner && !isPrincipalOfAcademy) {
      throw new LectureAccessDeniedException(lectureId, teacher.getId());
    }
  }
}
