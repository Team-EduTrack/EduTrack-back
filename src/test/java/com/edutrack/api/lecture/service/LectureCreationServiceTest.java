package com.edutrack.api.lecture.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.lecture.dto.LectureCreationRequest;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.service.LectureCreationService;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.AcademyMismatchException;
import com.edutrack.global.exception.ConflictException;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LectureCreationServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AcademyRepository academyRepository;

    @InjectMocks
    private LectureCreationService lectureCreationService;

    @Test
    @DisplayName("복수 요일로 강의 생성 성공")
    void createLecture_withMultipleDaysOfWeek_success() {
        // given
        Long principalId = 1L;
        Long teacherId = 2L;
        Long academyId = 10L;

        User principal = createMockUser(principalId, academyId, RoleType.PRINCIPAL);
        User teacher = createMockUser(teacherId, academyId, RoleType.TEACHER);
        Academy academy = createMockAcademy(academyId);

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setDescription("기초 수학 강의");
        request.setTeacherId(teacherId);
        request.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        Lecture savedLecture = new Lecture(
                academy, teacher, "수학 기초", "기초 수학 강의",
                request.getDaysOfWeek(), request.getStartDate(), request.getEndDate()
        );

        given(userRepository.findById(principalId)).willReturn(Optional.of(principal));
        given(userRepository.findById(teacherId)).willReturn(Optional.of(teacher));
        given(academyRepository.getReferenceById(academyId)).willReturn(academy);
        given(lectureRepository.save(any(Lecture.class))).willReturn(savedLecture);

        // when
        Long lectureId = lectureCreationService.createLecture(principalId, request);

        // then
        assertThat(lectureId).isNotNull();
        verify(lectureRepository).save(any(Lecture.class));
    }

    @Test
    @DisplayName("요일이 비어있으면 ConflictException 발생")
    void createLecture_emptyDaysOfWeek_throwsConflictException() {
        // given
        Long principalId = 1L;
        Long academyId = 10L;

        User principal = createMockUser(principalId, academyId, RoleType.PRINCIPAL);

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setTeacherId(2L);
        request.setDaysOfWeek(List.of()); // 빈 리스트
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        given(userRepository.findById(principalId)).willReturn(Optional.of(principal));

        // when & then
        assertThatThrownBy(() -> lectureCreationService.createLecture(principalId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("강의 요일은 최소 1개 이상 선택해야 합니다");
    }

    @Test
    @DisplayName("중복된 요일이 있으면 ConflictException 발생")
    void createLecture_duplicateDaysOfWeek_throwsConflictException() {
        // given
        Long principalId = 1L;
        Long academyId = 10L;

        User principal = createMockUser(principalId, academyId, RoleType.PRINCIPAL);

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setTeacherId(2L);
        request.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)); // 중복
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        given(userRepository.findById(principalId)).willReturn(Optional.of(principal));

        // when & then
        assertThatThrownBy(() -> lectureCreationService.createLecture(principalId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("중복된 요일이 선택되었습니다");
    }

    @Test
    @DisplayName("원장이 존재하지 않으면 NotFoundException 발생")
    void createLecture_principalNotFound_throwsNotFoundException() {
        // given
        Long principalId = 999L;

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setTeacherId(2L);
        request.setDaysOfWeek(List.of(DayOfWeek.MONDAY));
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        given(userRepository.findById(principalId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> lectureCreationService.createLecture(principalId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("강의 생성 권한을 가진 사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("강사가 다른 학원 소속이면 AcademyMismatchException 발생")
    void createLecture_teacherFromDifferentAcademy_throwsAcademyMismatchException() {
        // given
        Long principalId = 1L;
        Long teacherId = 2L;
        Long principalAcademyId = 10L;
        Long teacherAcademyId = 20L; // 다른 학원

        User principal = createMockUser(principalId, principalAcademyId, RoleType.PRINCIPAL);
        User teacher = createMockUser(teacherId, teacherAcademyId, RoleType.TEACHER);

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setTeacherId(teacherId);
        request.setDaysOfWeek(List.of(DayOfWeek.MONDAY));
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        given(userRepository.findById(principalId)).willReturn(Optional.of(principal));
        given(userRepository.findById(teacherId)).willReturn(Optional.of(teacher));

        // when & then
        assertThatThrownBy(() -> lectureCreationService.createLecture(principalId, request))
                .isInstanceOf(AcademyMismatchException.class)
                .hasMessageContaining("지정된 강사는 이 학원 소속이 아닙니다");
    }

    @Test
    @DisplayName("강사 권한이 없으면 ForbiddenException 발생")
    void createLecture_teacherWithoutRole_throwsForbiddenException() {
        // given
        Long principalId = 1L;
        Long teacherId = 2L;
        Long academyId = 10L;

        User principal = createMockUser(principalId, academyId, RoleType.PRINCIPAL);
        User teacher = createMockUser(teacherId, academyId, RoleType.STUDENT); // 학생 역할

        LectureCreationRequest request = new LectureCreationRequest();
        request.setTitle("수학 기초");
        request.setTeacherId(teacherId);
        request.setDaysOfWeek(List.of(DayOfWeek.MONDAY));
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusMonths(3));

        given(userRepository.findById(principalId)).willReturn(Optional.of(principal));
        given(userRepository.findById(teacherId)).willReturn(Optional.of(teacher));

        // when & then
        assertThatThrownBy(() -> lectureCreationService.createLecture(principalId, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("지정된 사용자는 강사 권한이 없습니다");
    }

    // Helper methods
    private User createMockUser(Long userId, Long academyId, RoleType roleType) {
        User user = org.mockito.Mockito.mock(User.class);
        Academy academy = createMockAcademy(academyId);

        given(user.getId()).willReturn(userId);
        given(user.getAcademy()).willReturn(academy);
        given(user.hasRole(roleType)).willReturn(true);
        given(user.hasRole(org.mockito.ArgumentMatchers.any(RoleType.class)))
                .willAnswer(invocation -> {
                    RoleType arg = invocation.getArgument(0);
                    return arg == roleType;
                });

        return user;
    }

    private Academy createMockAcademy(Long academyId) {
        Academy academy = org.mockito.Mockito.mock(Academy.class);
        given(academy.getId()).willReturn(academyId);
        return academy;
    }
}
