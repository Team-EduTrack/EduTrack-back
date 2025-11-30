package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.dto.ExamStartResponse;
import com.edutrack.domain.exam.dto.ExamSubmitRequest;
import com.edutrack.domain.exam.dto.ExamSubmitResponse;

public interface ExamStudentService {

    ExamStartResponse startExam(Long examId, Long studentId);
    ExamSubmitResponse submitExam(Long examId, Long studentId, ExamSubmitRequest request);
}
