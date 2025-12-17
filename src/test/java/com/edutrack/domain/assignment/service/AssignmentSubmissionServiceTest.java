package com.edutrack.domain.assignment.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionStudentViewResponse;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssignmentSubmissionServiceTest {

    @Mock
    AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AssignmentSubmissionService assignmentSubmissionService;

    @Test
    @DisplayName("학생은 자신의 과제 제출 상세 조회 시 제출 완료 상태를 정상 조회한다")
    void getMySubmission_submitted_success() {
        // given
        Long academyId = 1L;
        Long assignmentId = 10L;
        Long studentId = 3L;

        // 학원
        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        // 강의
        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "title", "영어 회화 1");
        ReflectionTestUtils.setField(lecture, "academy", academy);

        // 강사
        User teacher = createInstance(User.class);
        ReflectionTestUtils.setField(teacher, "name", "김선생");

        // 과제
        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", assignmentId);
        ReflectionTestUtils.setField(assignment, "title", "문장 완성 과제");
        ReflectionTestUtils.setField(assignment, "description", "현재완료 문장 5개 작성하기");
        ReflectionTestUtils.setField(assignment, "lecture", lecture);
        ReflectionTestUtils.setField(assignment, "teacher", teacher);

        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.of(assignment));

        // 제출물
        AssignmentSubmission submission =
                new AssignmentSubmission(assignment, createInstance(User.class), "/files/report1.pdf");
        ReflectionTestUtils.setField(submission, "id", 999L);
        ReflectionTestUtils.setField(submission, "score", 85);
        ReflectionTestUtils.setField(submission, "feedback", "전체적으로 잘 작성했습니다.");

        given(assignmentSubmissionRepository
                .findByAssignment_IdAndStudent_Id(assignmentId, studentId))
                .willReturn(Optional.of(submission));

        // when
        AssignmentSubmissionStudentViewResponse result =
                assignmentSubmissionService.getMySubmission(academyId, studentId, assignmentId);

        // then
        assertThat(result.isSubmitted()).isTrue();
        assertThat(result.getSubmissionId()).isEqualTo(999L);
        assertThat(result.getLectureName()).isEqualTo("영어 회화 1");
        assertThat(result.getTeacherName()).isEqualTo("김선생");
        assertThat(result.getAssignmentTitle()).isEqualTo("문장 완성 과제");
        assertThat(result.getFilePath()).isEqualTo("/files/report1.pdf");
        assertThat(result.getScore()).isEqualTo(85);
        assertThat(result.getFeedback()).isEqualTo("전체적으로 잘 작성했습니다.");
    }

    @Test
    @DisplayName("학생이 과제를 제출하지 않았으면 submitted=false 로 정상 반환한다")
    void getMySubmission_notSubmitted_returnsSubmittedFalse() {
        // given
        Long academyId = 1L;
        Long assignmentId = 10L;
        Long studentId = 3L;

        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);
        User teacher = createInstance(User.class);
        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "academy", academy);
        ReflectionTestUtils.setField(lecture, "title", "영어 회화 1");
        ReflectionTestUtils.setField(teacher, "name", "김선생");
        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", assignmentId);
        ReflectionTestUtils.setField(assignment, "lecture", lecture);
        ReflectionTestUtils.setField(assignment, "teacher", teacher);
        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.of(assignment));

        given(assignmentSubmissionRepository
                .findByAssignment_IdAndStudent_Id(assignmentId, studentId))
                .willReturn(Optional.empty());

        // when
        AssignmentSubmissionStudentViewResponse result =
                assignmentSubmissionService.getMySubmission(academyId, studentId, assignmentId);

        // then
        assertThat(result.isSubmitted()).isFalse();
        assertThat(result.getSubmissionId()).isNull();
        assertThat(result.getFilePath()).isNull();
        assertThat(result.getScore()).isNull();
        assertThat(result.getFeedback()).isNull();
    }

    @Test
    @DisplayName("요청한 academyId와 과제가 속한 학원이 다르면 ForbiddenException 이 발생한다")
    void getMySubmission_wrongAcademy() {
        // given
        Long requestAcademyId = 1L;
        Long assignmentId = 10L;
        Long studentId = 3L;

        Academy otherAcademy = new Academy("다른 학원", "ACAD999", null);
        ReflectionTestUtils.setField(otherAcademy, "id", 2L);

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "academy", otherAcademy);

        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "lecture", lecture);

        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.of(assignment));

        // when & then
        assertThatThrownBy(() ->
                assignmentSubmissionService.getMySubmission(requestAcademyId, studentId, assignmentId)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("존재하지 않는 과제 ID로 조회하면 NotFoundException 이 발생한다")
    void getMySubmission_assignmentNotFound() {
        // given
        given(assignmentRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                assignmentSubmissionService.getMySubmission(1L, 3L, 999L)
        ).isInstanceOf(NotFoundException.class);
    }

    // ===== util =====
    private <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}