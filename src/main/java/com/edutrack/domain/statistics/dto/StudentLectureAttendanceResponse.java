package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 학생 강의별 월간 출석 현황 응답 DTO
 * 복수 요일 지원을 포함합니다.
 */
@Getter
@Builder
public class StudentLectureAttendanceResponse {
    private final Long lectureId;
    private final String lectureName;
    private final int year;
    private final int month;
    private final List<LocalDate> attendedDates;
    private final int totalClassDays;
    private final int attendedDays;
    private final double attendanceRate;
    private final double otherStudentsAvgAttendanceRate;
}
