package com.edutrack.api.student.dto;
import java.time.LocalDate;

public record MyLectureResponse(
        Long lectureId,
        String lectureTitle,
        String teacherName,
        LocalDate startDate,
        LocalDate endDate
) {}