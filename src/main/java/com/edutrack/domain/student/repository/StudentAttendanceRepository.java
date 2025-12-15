package com.edutrack.domain.student.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.user.entity.User;

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

    /**
     * 학생의 특정 월 출석 기록 조회 (출석한 것만)
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
           "AND a.status = true " +
           "AND YEAR(a.date) = :year AND MONTH(a.date) = :month")
    List<Attendance> findByStudentIdAndYearAndMonth(
            @Param("studentId") Long studentId,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 여러 학생의 특정 월 출석 기록 조회 (출석한 것만)
     */
    @Query("SELECT a FROM Attendance a WHERE a.student.id IN :studentIds " +
           "AND a.status = true " +
           "AND YEAR(a.date) = :year AND MONTH(a.date) = :month")
    List<Attendance> findByStudentIdsAndYearAndMonth(
            @Param("studentIds") List<Long> studentIds,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 여러 학생의 특정 날짜 목록에 대한 출석 기록 배치 조회
     */
    @Query("""
            SELECT a 
            FROM Attendance a
            WHERE a.student.id IN :studentIds 
              AND a.date IN :dates
              AND a.status = true
            """)
    List<Attendance> findAllByStudentIdsAndDates(
            @Param("studentIds") List<Long> studentIds,
            @Param("dates") List<LocalDate> dates
    );

    /**
     * 학생의 특정기간 출석 기록 조회
     * 복수 요일 지원과 함께 사용됩니다.
     */
    List<Attendance> findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 여러 학생의 특정기간 출석 기록 배치 조회
     */
    List<Attendance> findByStudentIdInAndDateBetweenAndStatusTrueOrderByDateAsc(
            List<Long> studentIds,
            LocalDate startDate,
            LocalDate endDate
    );
}