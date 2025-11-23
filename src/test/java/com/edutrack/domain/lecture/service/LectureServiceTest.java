package com.edutrack.domain.lecture.service;

import com.edutrack.domain.lecture.dto.LectureForTeacherResponseDto;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.lectureStudent.entity.LectureStudent;
import com.edutrack.domain.lecture.lectureStudent.repository.LectureStudentRepository;
import com.edutrack.domain.lecture.repository.LectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

  @Mock
  private LectureRepository lectureRepository;

  @Mock
  private LectureStudentRepository lectureStudentRepository;

  @InjectMocks
  private LectureService lectureService;

  private Lecture lecture1;
  private Lecture lecture2;

  @BeforeEach
  void setUp() {
    // Mockito mock for Lecture entities
    lecture1 = org.mockito.Mockito.mock(Lecture.class);
    lecture2 = org.mockito.Mockito.mock(Lecture.class);

    // Set IDs and titles
    given(lecture1.getId()).willReturn(1L);
    given(lecture2.getId()).willReturn(2L);
    given(lecture1.getTitle()).willReturn("강의1");
    given(lecture2.getTitle()).willReturn("강의2");
  }

  @Test
  void getLecturesByTeacherId_shouldReturnListWithCorrectStudentCount() {
    Long teacherId = 1L;

    // Mock lectureRepository
    given(lectureRepository.findAllByTeacherId(teacherId))
        .willReturn(List.of(lecture1, lecture2));

    // Mock lectureStudentRepository
    // 예: lecture1에 학생 2명, lecture2에 학생 1명
    LectureStudent ls1 = org.mockito.Mockito.mock(LectureStudent.class);
    LectureStudent ls2 = org.mockito.Mockito.mock(LectureStudent.class);
    LectureStudent ls3 = org.mockito.Mockito.mock(LectureStudent.class);

    given(ls1.getLecture()).willReturn(lecture1);
    given(ls2.getLecture()).willReturn(lecture1);
    given(ls3.getLecture()).willReturn(lecture2);

    given(lectureStudentRepository.findAllStudentsByLectureIdIn(List.of(1L, 2L)))
        .willReturn(List.of(ls1, ls2, ls3));

    // 서비스 호출
    List<LectureForTeacherResponseDto> result = lectureService.getLecturesByTeacherId(teacherId);

    // 검증
    assertThat(result).hasSize(2);

    Map<Long, Integer> countByLecture = result.stream()
        .collect(java.util.stream.Collectors.toMap(
            LectureForTeacherResponseDto::getLectureId,
            LectureForTeacherResponseDto::getStudentCount
        ));

    assertThat(countByLecture.get(1L)).isEqualTo(2);
    assertThat(countByLecture.get(2L)).isEqualTo(1);
  }

}