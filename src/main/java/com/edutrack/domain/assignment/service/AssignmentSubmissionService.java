package com.edutrack.domain.assignment.service;

import com.edutrack.domain.assignment.dto.AssignmentSubmitRequest;
import com.edutrack.domain.assignment.dto.AssignmentSubmitResponse;
import com.edutrack.domain.assignment.dto.PresignedUrlRequest;
import com.edutrack.domain.assignment.dto.PresignedUrlResponse;
import com.edutrack.domain.assignment.dto.*;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.user.entity.RoleType;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import com.edutrack.global.s3.S3PresignedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class AssignmentSubmissionService {

  private final S3PresignedService s3PresignedService;
  private final AssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository assignmentSubmissionRepository;
  private final UserRepository userRepository;

  private final String BUCKET_URL = "https://edutrack-bucket.s3.amazonaws.com/";

  // Presigned URL 생성
  public PresignedUrlResponse createPresignedUrl(Long assignmentId, PresignedUrlRequest request) {

    String dir = "assignments/" + assignmentId;

    // Presigned URL 생성
    PresignedPutObjectRequest presigned = s3PresignedService.createPresignedUrl(dir,
        request.getFileName());

    // key 추출
    String key = presigned.url().getPath().substring(1);

    return new PresignedUrlResponse(
        presigned.url().toString(), key
    );

  }

  // 과제 제출 저장
  @Transactional
  public AssignmentSubmitResponse submit(Long assignmentId, Long studentId, AssignmentSubmitRequest request) {

    Assignment assignment = assignmentRepository.findById(assignmentId)
        .orElseThrow(() -> new RuntimeException("과제가 존재하지 않습니다."));

    User student = userRepository.findById(studentId)
        .orElseThrow(() -> new RuntimeException("학생이 존재하지 않습니다."));

        if (assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Id(assignmentId, studentId)) {
            throw new RuntimeException("이미 제출한 과제입니다.");
        }

    // S3 접근 가능한 최종 URL
    String fileUrl = BUCKET_URL + request.getFileKey();

    AssignmentSubmission submission = new AssignmentSubmission(
        assignment,
        student,
        fileUrl
    );

        assignmentSubmissionRepository.save(submission);
        return new AssignmentSubmitResponse(
                submission.getId(),
                "과제 제출이 성공적으로 완료되었습니다.",
                fileUrl
        );
    }

    @Transactional(readOnly = true)
    public AssignmentSubmissionStudentViewResponse getMySubmission(
            Long academyId,
            Long studentId,
            Long assignmentId
    ) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("지정된 과제를 찾을 수 없습니다. ID: " + assignmentId));

        Long assignmentAcademyId = assignment.getLecture().getAcademy().getId();
        if (!assignmentAcademyId.equals(academyId)) {
            throw new ForbiddenException("해당 학원의 과제가 아닙니다.");
        }

        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                .orElseThrow(() -> new NotFoundException("과제 제출 내역을 찾을 수 없습니다."));

        User student = submission.getStudent();

        return AssignmentSubmissionStudentViewResponse.builder()
                .submissionId(submission.getId())
                .assignmentId(assignment.getId())
                .lectureName(assignment.getLecture().getTitle())   // 강의명
                .teacherName(assignment.getTeacher().getName())    // 강사 이름
                .studentLoginId(student.getLoginId())              // 학생 정보(본인)
                .studentName(student.getName())
                .assignmentTitle(assignment.getTitle())
                .assignmentDescription(assignment.getDescription())
                .filePath(submission.getFilePath())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .build();
    }

    /**
     * 🔹 강사용 – 제출 상세 조회
     */
    @Transactional(readOnly = true)
    public AssignmentSubmissionTeacherViewResponse getSubmissionForTeacher(
            Long academyId,
            Long teacherId,
            Long assignmentId,
            Long submissionId
    ) {
        //강사 조회
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("채점 권한이 있는 사용자를 찾을 수 없습니다."));

        //과제 조회
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("지정된 과제를 찾을 수 없습니다. ID: " + assignmentId));

        //학원 검증
        Long assignmentAcademyId = assignment.getLecture().getAcademy().getId();
        if (!assignmentAcademyId.equals(academyId)) {
            throw new ForbiddenException("해당 학원의 과제가 아닙니다.");
        }


        boolean isOwnerTeacher = assignment.getTeacher().getId().equals(teacherId);

        if (!isOwnerTeacher) {
            throw new ForbiddenException("해당 과제에 대한 조회 권한이 없습니다.");
        }

        //제출물 조회
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("과제 제출 내역을 찾을 수 없습니다. ID: " + submissionId));

        //이 제출물이 정말 이 과제의 것인지 검증 (URL 장난 방지)
        if (!submission.getAssignment().getId().equals(assignmentId)) {
            throw new ForbiddenException("해당 과제의 제출물이 아닙니다.");
        }

        User student = submission.getStudent();

        return AssignmentSubmissionTeacherViewResponse.builder()
                .submissionId(submission.getId())
                .assignmentId(assignment.getId())
                .lectureName(assignment.getLecture().getTitle())   // 강의 명
                .teacherName(assignment.getTeacher().getName())    // 강사 이름
                .studentLoginId(student.getLoginId())
                .studentName(student.getName())
                .assignmentTitle(assignment.getTitle())
                .assignmentDescription(assignment.getDescription())
                .filePath(submission.getFilePath())                // ERD: file_path
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .build();
    }

    /**
     * 🔹 강사용 – 채점 + 피드백 저장
     */
    public AssignmentGradeResponse gradeSubmission(
            Long academyId,
            Long teacherId,
            Long assignmentId,
            Long submissionId,
            AssignmentGradeRequest request
    ) {

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("채점 권한이 있는 사용자를 찾을 수 없습니다."));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("지정된 과제를 찾을 수 없습니다. ID: " + assignmentId));

        Long assignmentAcademyId = assignment.getLecture().getAcademy().getId();
        if (!assignmentAcademyId.equals(academyId)) {
            throw new ForbiddenException("해당 학원의 과제가 아닙니다.");
        }

        boolean isPrincipal = teacher.hasRole(RoleType.PRINCIPAL);
        boolean isOwnerTeacher = assignment.getTeacher().getId().equals(teacherId);

        if (!isPrincipal && !isOwnerTeacher) {
            throw new ForbiddenException("해당 과제에 대한 채점 권한이 없습니다.");
        }

        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("과제 제출 내역을 찾을 수 없습니다. ID: " + submissionId));

        if (!submission.getAssignment().getId().equals(assignmentId)) {
            throw new ForbiddenException("해당 과제의 제출물이 아닙니다.");
        }

        //score, feedback 수정
        submission.grade(request.getScore(), request.getFeedback());
        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);

        return AssignmentGradeResponse.builder()
                .submissionId(saved.getId())
                .assignmentId(saved.getAssignment().getId())
                .studentId(saved.getStudent().getId())
                .score(saved.getScore())
                .feedback(saved.getFeedback())
                .build();
    }
}
