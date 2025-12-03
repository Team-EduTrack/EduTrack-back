package com.edutrack.domain.exam.service;

import com.edutrack.domain.exam.dto.ExamStartResponse;

public interface ExamStudentService {

    ExamStartResponse startExam(Long examId, Long studentId);
}
