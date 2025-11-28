package com.edutrack.domain.assignment.service;

import com.edutrack.domain.assignment.dto.AssignmentSubmitRequest;
import com.edutrack.domain.assignment.dto.AssignmentSubmitResponse;
import com.edutrack.domain.assignment.dto.PresignedUrlRequest;
import com.edutrack.domain.assignment.dto.PresignedUrlResponse;
import com.edutrack.domain.assignment.entity.Assignment;
import com.edutrack.domain.assignment.entity.AssignmentSubmission;
import com.edutrack.domain.assignment.repository.AssignmentRepository;
import com.edutrack.domain.assignment.repository.AssignmentSubmissionRepository;
import com.edutrack.domain.user.entity.User;
import com.edutrack.domain.user.repository.UserRepository;
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

}
