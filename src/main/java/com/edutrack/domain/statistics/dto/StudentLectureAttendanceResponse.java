package com.edutrack.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

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
}

