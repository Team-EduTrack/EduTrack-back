package com.edutrack.api.student.repository;

import com.edutrack.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
// 출석
public interface StudentAttendanceRepository
        extends JpaRepository<Attendance, Long> {

    @Query(value = """
            SELECT COUNT(*)
            FROM attendance a
            WHERE a.user_id = :studentId
              AND a.attendance_date = :date
            """, nativeQuery = true)
    int countTodayAttendance(@Param("studentId") Long studentId,
                             @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO attendance (attendance_date, status, user_id)
            VALUES (:date, :status, :studentId)
            """, nativeQuery = true)
    void insertAttendance(@Param("studentId") Long studentId,
                          @Param("date") LocalDate date,
                          @Param("status") boolean status);
}