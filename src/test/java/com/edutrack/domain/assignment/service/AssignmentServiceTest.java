package com.edutrack.domain.assignment.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.assignment.dto.AssignmentListResponse;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    LectureRepository lectureRepository;

    @InjectMocks
    AssignmentService assignmentService;

    @Test
    @DisplayName("강의 ID로 과제를 조회하면 제목과 날짜만 담긴 DTO 리스트를 반환한다")
    void getAssignmentsForLecture_success() {
        // given
        Long academyId = 1L;
        Long lectureId = 10L;

        // 🔹 Academy 생성 (엔티티 구조에 맞게)
        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        // 🔹 Lecture 생성 (기본 생성자가 protected일 수 있으므로 리플렉션으로 생성)
        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", lectureId);
        ReflectionTestUtils.setField(lecture, "academy", academy);

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.of(lecture));

        // 🔹 Assignment 생성 (마찬가지로 리플렉션 사용)
        Assignment a1 = createInstance(Assignment.class);
        ReflectionTestUtils.setField(a1, "id", 100L);
        ReflectionTestUtils.setField(a1, "title", "단어 테스트 과제");
        ReflectionTestUtils.setField(a1, "startDate",
                LocalDateTime.of(2025, 11, 27, 0, 0));
        ReflectionTestUtils.setField(a1, "endDate",
                LocalDateTime.of(2025, 11, 29, 23, 59));

        given(assignmentRepository.findByLectureId(lectureId))
                .willReturn(List.of(a1));

        // when
        List<AssignmentListResponse> result =
                assignmentService.getAssignmentsForLecture(academyId, lectureId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignmentId()).isEqualTo(100L);
        assertThat(result.get(0).getTitle()).isEqualTo("단어 테스트 과제");
        assertThat(result.get(0).getStartDate()).isEqualTo(
                LocalDateTime.of(2025, 11, 27, 0, 0)
        );
        assertThat(result.get(0).getEndDate()).isEqualTo(
                LocalDateTime.of(2025, 11, 29, 23, 59)
        );
    }

    @Test
    @DisplayName("없는 강의 ID로 조회하면 NotFoundException 이 발생한다")
    void getAssignmentsForLecture_lectureNotFound() {
        // given
        Long academyId = 1L;
        Long lectureId = 999L;

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                assignmentService.getAssignmentsForLecture(academyId, lectureId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("학원 ID가 강의의 학원과 다르면 ForbiddenException 이 발생한다")
    void getAssignmentsForLecture_wrongAcademy() {
        // given
        Long requestAcademyId = 1L;   // 요청에 들어온 academyId
        Long lectureId = 10L;

        // 🔹 다른 학원 ID를 가진 Academy
        Academy otherAcademy = new Academy("다른 학원", "ACAD999", null);
        ReflectionTestUtils.setField(otherAcademy, "id", 2L); // 1L과 다른 값

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", lectureId);
        ReflectionTestUtils.setField(lecture, "academy", otherAcademy);

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.of(lecture));

        // when & then
        assertThatThrownBy(() ->
                assignmentService.getAssignmentsForLecture(requestAcademyId, lectureId)
        ).isInstanceOf(ForbiddenException.class);
    }

    /**
     * 엔티티 기본 생성자가 protected인 경우에도 인스턴스를 만들기 위한 유틸 메서드
     */
    private <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("엔티티 인스턴스 생성 실패: " + clazz.getName(), e);
        }
    }
}