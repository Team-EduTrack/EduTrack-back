package com.edutrack.domain.lecture.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.lecture.dto.LectureCreationRequest;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureCreationService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final AcademyRepository academyRepository;

    public Long createLecture(Long principalUserId, LectureCreationRequest request) {

        // 1. 강의 생성 권한 주체(원장)를 조회하고 검증 (ACADEMY ID 추출)
        User principal = userRepository.findById(principalUserId)
                .orElseThrow(() -> new NotFoundException("강의 생성 권한을 가진 사용자를 찾을 수 없습니다."));

        Long principalAcademyId = principal.getAcademy().getId();

        // 2. 강사 유효성 검증
        User teacher = validateTeacherAndFetch(principalAcademyId, request.getTeacherId());

        // 3. 날짜 유효성 검증
        validateDates(request.getStartDate(), request.getEndDate());

        // 4. Lecture 엔티티 생성 및 저장
        Academy academy = academyRepository.getReferenceById(principalAcademyId);

        Lecture lecture = Lecture.builder()
                .academy(academy)
                .teacher(teacher)
                .title(request.getTitle())
                .description(request.getDescription())
                .dayOfWeek(request.getDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Lecture savedLecture = lectureRepository.save(lecture);
        return savedLecture.getId();
    }


    @Transactional(readOnly = true)
    private User validateTeacherAndFetch(Long principalAcademyId, Long teacherId) {

        // 1. 강사 존재 여부 확인 (이 로직은 그대로 유지)
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("지정된 강사 계정을 찾을 수 없습니다."));

        // 2. 조건: 강사 역할(TEACHER)을 가지고 있는가?
        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new ForbiddenException("지정된 사용자는 강사 권한이 없습니다.");
        }

        // 3. 조건: 강사가 원장과 동일한 학원 소속인가?
        if (teacher.getAcademy() == null || !teacher.getAcademy().getId().equals(principalAcademyId)) {
            throw new ForbiddenException("지정된 강사는 이 학원 소속이 아닙니다.");
        }

        return teacher;
    }

    @Transactional(readOnly = true)
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ConflictException("강의 시작일이 종료일보다 늦을 수 없습니다.");
        }
    }
}