package com.edutrack.domain.assignment.service;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.assignment.dto.AssignmentCreateRequest;
import com.edutrack.domain.assignment.dto.AssignmentCreateResponse;
import com.edutrack.domain.assignment.dto.AssignmentListResponse;
import com.edutrack.domain.assignment.dto.AssignmentSubmissionStatus;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudentId;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edutrack.global.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final LectureStudentRepository lectureStudentRepository;
    /**
     * 강사가 특정 강의에 과제를 생성
     */
    @Transactional
    public AssignmentCreateResponse createAssignment(
            Long academyId,
            Long lectureId,
            Long teacherId,
            AssignmentCreateRequest req
    ) {
        // 강사 조회
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("강사를 찾을 수 없습니다. ID: " + teacherId));

        // 강사 권한 확인
        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new ForbiddenException("강사만 과제를 생성할 수 있습니다.");
        }

        // 학원 소속 확인
        if (!teacher.getAcademy().getId().equals(academyId)) {
            throw new ForbiddenException("해당 학원 소속 강사만 과제를 생성할 수 있습니다.");
        }

        // 강의 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));

        // 담당 강사 검증
        if (!lecture.getTeacher().getId().equals(teacherId)) {
            throw new ForbiddenException("해당 강의의 담당 강사만 과제를 생성할 수 있습니다.");
        }

        // 과제 생성
        Assignment assignment = Assignment.create(
                lecture,
                teacher,
                req.getTitle(),
                req.getDescription(),
                req.getStartDate(),
                req.getEndDate()
        );

        Assignment saved = assignmentRepository.save(assignment);

        return AssignmentCreateResponse.builder()
                .assignmentId(saved.getId())
                .build();
    }

    /**
     * 학생용 – 특정 강의의 과제 리스트 조회 (제목 + 마감일 + 제출 여부)
     */
    @Transactional(readOnly = true)
    public List<AssignmentListResponse> getAssignmentsForLecture(
            Long academyId,
            Long studentId,
            Long lectureId
    ) {
        // 강의 확인
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("지정된 강의를 찾을 수 없습니다. ID: " + lectureId));

        // 학원 검증
        Academy academy = lecture.getAcademy();
        if (!academy.getId().equals(academyId)) {
            throw new ForbiddenException("해당 학원에 속하지 않은 강의입니다.");
        }

        // 수강 여부 검증
        boolean enrolled = lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId);

        if (!enrolled) {
            throw new ForbiddenException("해당 강의를 수강 중인 학생만 과제 목록을 조회할 수 있습니다.");
        }

        // 과제 조회
        List<Assignment> assignments = assignmentRepository.findByLectureId(lectureId);

        // 제출 여부 포함하여 DTO 변환
        return assignments.stream()
                .map(assignment -> {
                    boolean submitted = assignmentSubmissionRepository
                            .existsByAssignment_IdAndStudent_Id(assignment.getId(), studentId);

                    AssignmentSubmissionStatus status =
                            submitted ? AssignmentSubmissionStatus.SUBMITTED : AssignmentSubmissionStatus.NOT_SUBMITTED;

                    return AssignmentListResponse.builder()
                            .assignmentId(assignment.getId())
                            .title(assignment.getTitle())
                            .endDate(assignment.getEndDate())
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }
}