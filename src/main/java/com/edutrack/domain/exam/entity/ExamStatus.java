package com.edutrack.domain.exam.entity;

//시험 자체의 상태?
public enum ExamStatus {
    DRAFT,      // 임시 저장 (문제 출제 중)
    PUBLISHED,  // 공개됨 (학생 응시 가능)
    CLOSED      // 종료됨 (학생 응시 불가)
}
