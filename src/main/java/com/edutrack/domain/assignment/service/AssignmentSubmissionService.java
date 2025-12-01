package com.edutrack.domain.assignment.service;

import com.edutrack.domain.assignment.dto.*;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
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

        if (assignmentSubmissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
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

        return AssignmentSubmissionStudentViewResponse.builder()
                .submissionId(submission.getId())
                .assignmentId(assignment.getId())
                .assignmentTitle(assignment.getTitle())
                .assignmentDescription(assignment.getDescription())
                .filePath(submission.getFilePath())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .build();
    }
}
