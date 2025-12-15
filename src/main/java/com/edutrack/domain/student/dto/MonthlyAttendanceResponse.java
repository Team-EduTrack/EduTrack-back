package com.edutrack.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MonthlyAttendanceResponse {
    private final Long lectureId;
    private final String lectureName;
    private final Integer year;
    private final Integer month;
    private final List<String> attendedDates; // 출석한 날짜들 (YYYY-MM-DD 형식)
    private final Integer totalClassDays; // 해당 월의 총 수업일 수
    private final Integer attendedDays; // 출석한 일수
    private final Double attendanceRate; // 출석률 (퍼센트)
    private final Double otherStudentsAvgAttendanceRate; // 다른 수강생 평균 출석률
}
