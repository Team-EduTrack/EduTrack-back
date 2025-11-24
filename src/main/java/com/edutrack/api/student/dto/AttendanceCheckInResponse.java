package com.edutrack.api.student.dto;

import java.time.LocalDate;

public record AttendanceCheckInResponse(
        Long studentId,
        LocalDate attendanceDate,
        boolean alreadyCheckedIn
) {}
