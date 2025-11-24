package com.edutrack.api.lecture.service;

import com.edutrack.api.lecture.dto.LectureCreationRequest;
import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.academy.AcademyRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ConflictException;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureCreationService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final AcademyRepository academyRepository;

    public Long createLecture(Long principalAcademyId, LectureCreationRequest request) {

        //강사 유효성 검증
        User teacher = validateTeacherAndFetch(principalAcademyId, request.getTeacherId());

        // 날짜 유효성 검증
        validateDates(request.getStartDate(), request.getEndDate());

        Academy academy = academyRepository.getReferenceById(principalAcademyId);


        Lecture lecture = new Lecture(
                academy,
                teacher,
                request.getTitle(),
                request.getDescription(),
                request.getDate(),
                request.getStartDate(),
                request.getEndDate()
        );

        Lecture savedLecture = lectureRepository.save(lecture);
        return savedLecture.getId();
    }


    @Transactional(readOnly = true)
    private User validateTeacherAndFetch(Long principalAcademyId, Long teacherId) {


        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("지정된 강사 계정을 찾을 수 없습니다."));


        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new ForbiddenException("지정된 사용자는 강사 권한이 없습니다.");
        }

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