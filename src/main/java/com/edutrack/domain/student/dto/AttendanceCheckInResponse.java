package com.edutrack.domain.student.dto;

import java.time.LocalDate;

public record AttendanceCheckInResponse(
        Long studentId,
        LocalDate attendanceDate,
        boolean alreadyCheckedIn
) {}
