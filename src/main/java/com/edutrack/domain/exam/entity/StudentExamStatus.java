package com.edutrack.domain.exam.entity;

//학생의 시험 응시 상태
public enum StudentExamStatus {
    IN_PROGRESS, // 응시 중
    SUBMITTED,   // 제출 완료 (채점 전)
    GRADED       // 채점 완료 (결과 확인 가능)
}
