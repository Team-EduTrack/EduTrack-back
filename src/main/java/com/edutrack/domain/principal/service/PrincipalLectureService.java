package com.edutrack.domain.principal.service;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.principal.dto.PrincipalLectureResponse;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrincipalLectureService {

    private final LectureRepository lectureRepository;
    private final LectureStudentRepository lectureStudentRepository;
    private final UserRepository userRepository;

    /**
     * 원장의 학원 내 전체 강의 조회 (페이지네이션)
     * - 원장만 호출 가능
     * - 해당 학원에 속한 모든 강의 조회
     */
    public Page<PrincipalLectureResponse> getLecturesByAcademy(Long principalId, Pageable pageable) {
        // 원장 조회
        User principal = userRepository.findById(principalId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. ID: " + principalId));

        // 원장 권한 확인
        if (!principal.hasRole(RoleType.PRINCIPAL)) {
            throw new ForbiddenException("원장만 접근할 수 있습니다.");
        }

        // 학원 ID 확인
        if (principal.getAcademy() == null) {
            throw new ForbiddenException("소속 학원이 없습니다.");
        }

        Long academyId = principal.getAcademy().getId();

        // 강의 목록 조회 (페이지네이션)
        Page<Lecture> lectures = lectureRepository.findAllByAcademyId(academyId, pageable);

        // DTO 변환 (학생 수 포함)
        return lectures.map(lecture -> {
            int studentCount = lectureStudentRepository.countByLectureId(lecture.getId());
            return PrincipalLectureResponse.of(lecture, studentCount);
        });
    }
}

