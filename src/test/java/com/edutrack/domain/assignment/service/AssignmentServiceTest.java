package com.edutrack.domain.assignment.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.assignment.dto.AssignmentListResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionStatus;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
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

    @Mock
    AssignmentSubmissionRepository assignmentSubmissionRepository;

    @InjectMocks
    AssignmentService assignmentService;

    @Test
    @DisplayName("학생이 과제를 제출한 경우 상태는 SUBMITTED 이고 제목/마감일이 정상적으로 반환된다")
    void getAssignmentsForLecture_success_submitted() {
        // given
        Long academyId = 1L;
        Long lectureId = 10L;
        Long studentId = 3L;

        // Academy 세팅
        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        // Lecture 세팅
        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", lectureId);
        ReflectionTestUtils.setField(lecture, "academy", academy);

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.of(lecture));

        // Assignment 세팅
        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", 100L);
        ReflectionTestUtils.setField(assignment, "title", "단어 테스트 과제");
        ReflectionTestUtils.setField(assignment, "endDate",
                LocalDateTime.of(2025, 11, 29, 23, 59));

        given(assignmentRepository.findByLectureId(lectureId))
                .willReturn(List.of(assignment));

        // 제출 여부: 존재한다고 가정 → SUBMITTED
        given(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(100L, 3L))
                .willReturn(true);

        // when
        List<AssignmentListResponse> result =
                assignmentService.getAssignmentsForLecture(academyId, studentId, lectureId);

        // then
        assertThat(result).hasSize(1);
        AssignmentListResponse item = result.get(0);

        assertThat(item.getAssignmentId()).isEqualTo(100L);
        assertThat(item.getTitle()).isEqualTo("단어 테스트 과제");
        assertThat(item.getEndDate())
                .isEqualTo(LocalDateTime.of(2025, 11, 29, 23, 59));
        assertThat(item.getStatus())
                .isEqualTo(AssignmentSubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("학생이 과제를 제출하지 않은 경우 상태는 NOT_SUBMITTED 으로 반환된다")
    void getAssignmentsForLecture_notSubmitted() {
        // given
        Long academyId = 1L;
        Long lectureId = 10L;
        Long studentId = 3L;

        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", lectureId);
        ReflectionTestUtils.setField(lecture, "academy", academy);

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.of(lecture));

        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", 200L);
        ReflectionTestUtils.setField(assignment, "title", "문장 구조 과제");
        ReflectionTestUtils.setField(assignment, "endDate",
                LocalDateTime.of(2025, 12, 5, 23, 59));

        given(assignmentRepository.findByLectureId(lectureId))
                .willReturn(List.of(assignment));

        // 제출 여부: 존재하지 않는다고 가정 → NOT_SUBMITTED
        given(assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(200L, 3L))
                .willReturn(false);

        // when
        List<AssignmentListResponse> result =
                assignmentService.getAssignmentsForLecture(academyId, studentId, lectureId);

        // then
        assertThat(result).hasSize(1);
        AssignmentListResponse item = result.get(0);

        assertThat(item.getAssignmentId()).isEqualTo(200L);
        assertThat(item.getStatus())
                .isEqualTo(AssignmentSubmissionStatus.NOT_SUBMITTED);
    }

    @Test
    @DisplayName("없는 강의 ID로 조회하면 NotFoundException 이 발생한다")
    void getAssignmentsForLecture_lectureNotFound() {
        // given
        Long academyId = 1L;
        Long lectureId = 999L;
        Long studentId = 3L;

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                assignmentService.getAssignmentsForLecture(academyId, studentId, lectureId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("요청한 academyId와 강의가 속한 학원이 다르면 ForbiddenException 이 발생한다")
    void getAssignmentsForLecture_wrongAcademy() {
        // given
        Long requestAcademyId = 1L; // URL에 들어온 academyId
        Long lectureId = 10L;
        Long studentId = 3L;

        // 강의는 다른 학원에 속해 있음
        Academy otherAcademy = new Academy("다른 학원", "ACAD999", null);
        ReflectionTestUtils.setField(otherAcademy, "id", 2L);

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", lectureId);
        ReflectionTestUtils.setField(lecture, "academy", otherAcademy);

        given(lectureRepository.findById(lectureId))
                .willReturn(Optional.of(lecture));

        // when & then
        assertThatThrownBy(() ->
                assignmentService.getAssignmentsForLecture(requestAcademyId, studentId, lectureId)
        ).isInstanceOf(ForbiddenException.class);
    }

    /**
     * 기본 생성자가 protected 인 엔티티(Lecture, Assignment)를
     * 리플렉션으로 생성하기 위한 유틸 메서드
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