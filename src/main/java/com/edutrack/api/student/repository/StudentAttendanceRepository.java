package com.edutrack.api.student.repository;

import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 학생 출석 Repository
 * - 출석 체크 및 조회
 */
@Repository
public interface StudentAttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * 특정 날짜의 출석 기록 조회
     */
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);

    /**
     * 특정 날짜에 출석 기록이 존재하는지 확인
     */
    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    /**
     * 학생과 날짜로 출석 기록 조회
     */
    Optional<Attendance> findByStudentAndDate(User student, LocalDate date);
}