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
    @DisplayName("학생은 자신의 과제 제출 상세 조회 시 강의/강사/학생/과제/점수/피드백 정보를 모두 조회할 수 있다")
    void getMySubmission_success() {
        // given
        Long academyId = 1L;
        Long assignmentId = 10L;
        Long studentId = 3L;

        // 학원
        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        // 강의
        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", 100L);
        ReflectionTestUtils.setField(lecture, "title", "영어 회화 1");
        ReflectionTestUtils.setField(lecture, "academy", academy);

        // 강사
        User teacher = createInstance(User.class);
        ReflectionTestUtils.setField(teacher, "id", 20L);
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

        // 학생(본인)
        User student = createInstance(User.class);
        ReflectionTestUtils.setField(student, "id", studentId);
        ReflectionTestUtils.setField(student, "loginId", "student01");
        ReflectionTestUtils.setField(student, "name", "홍길동");

        // 제출물
        AssignmentSubmission submission =
                new AssignmentSubmission(assignment, student, "/files/report1.pdf");
        ReflectionTestUtils.setField(submission, "id", 999L);
        ReflectionTestUtils.setField(submission, "score", 85);
        ReflectionTestUtils.setField(submission, "feedback", "전체적으로 잘 작성했습니다.");

        given(assignmentSubmissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, studentId))
                .willReturn(Optional.of(submission));

        // when
        AssignmentSubmissionStudentViewResponse result =
                assignmentSubmissionService.getMySubmission(academyId, studentId, assignmentId);

        // then
        assertThat(result.getSubmissionId()).isEqualTo(999L);
        assertThat(result.getAssignmentId()).isEqualTo(assignmentId);

        // 강의/강사
        assertThat(result.getLectureName()).isEqualTo("영어 회화 1");
        assertThat(result.getTeacherName()).isEqualTo("김선생");

        // 학생 정보(본인)
        assertThat(result.getStudentLoginId()).isEqualTo("student01");
        assertThat(result.getStudentName()).isEqualTo("홍길동");

        // 과제 정보
        assertThat(result.getAssignmentTitle()).isEqualTo("문장 완성 과제");
        assertThat(result.getAssignmentDescription()).isEqualTo("현재완료 문장 5개 작성하기");

        // 제출/채점 정보
        assertThat(result.getFilePath()).isEqualTo("/files/report1.pdf");
        assertThat(result.getScore()).isEqualTo(85);
        assertThat(result.getFeedback()).isEqualTo("전체적으로 잘 작성했습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 과제 ID로 학생 제출 상세 조회 시 NotFoundException 이 발생한다")
    void getMySubmission_assignmentNotFound() {
        // given
        Long academyId = 1L;
        Long assignmentId = 999L;
        Long studentId = 3L;

        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                assignmentSubmissionService.getMySubmission(academyId, studentId, assignmentId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("요청한 academyId와 과제가 속한 학원이 다르면 ForbiddenException 이 발생한다")
    void getMySubmission_wrongAcademy() {
        // given
        Long requestAcademyId = 1L; // URL에 들어온 academyId
        Long assignmentId = 10L;
        Long studentId = 3L;

        // 다른 학원
        Academy otherAcademy = new Academy("다른 학원", "ACAD999", null);
        ReflectionTestUtils.setField(otherAcademy, "id", 2L);

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", 100L);
        ReflectionTestUtils.setField(lecture, "title", "영어 회화 1");
        ReflectionTestUtils.setField(lecture, "academy", otherAcademy);

        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", assignmentId);
        ReflectionTestUtils.setField(assignment, "lecture", lecture);

        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.of(assignment));

        // when & then
        assertThatThrownBy(() ->
                assignmentSubmissionService.getMySubmission(requestAcademyId, studentId, assignmentId)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("학생이 해당 과제를 제출하지 않았다면 NotFoundException 이 발생한다")
    void getMySubmission_submissionNotFound() {
        // given
        Long academyId = 1L;
        Long assignmentId = 10L;
        Long studentId = 3L;

        // 학원 + 강의 + 과제는 정상
        Academy academy = new Academy("테스트 학원", "ACAD001", null);
        ReflectionTestUtils.setField(academy, "id", academyId);

        Lecture lecture = createInstance(Lecture.class);
        ReflectionTestUtils.setField(lecture, "id", 100L);
        ReflectionTestUtils.setField(lecture, "title", "영어 회화 1");
        ReflectionTestUtils.setField(lecture, "academy", academy);

        Assignment assignment = createInstance(Assignment.class);
        ReflectionTestUtils.setField(assignment, "id", assignmentId);
        ReflectionTestUtils.setField(assignment, "lecture", lecture);

        given(assignmentRepository.findById(assignmentId))
                .willReturn(Optional.of(assignment));

        // 학생 제출이 없음
        given(assignmentSubmissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, studentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                assignmentSubmissionService.getMySubmission(academyId, studentId, assignmentId)
        ).isInstanceOf(NotFoundException.class);
    }

    /**
     * 기본 생성자가 protected 인 엔티티(Lecture, Assignment, User)를
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
