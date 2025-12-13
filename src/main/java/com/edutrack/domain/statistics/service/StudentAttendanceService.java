package com.edutrack.domain.statistics.service;

import com.edutrack.api.student.repository.StudentAttendanceRepository;
import com.edutrack.domain.attendance.entity.Attendance;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.lecture.entity.LectureStudent;
import com.edutrack.domain.lecture.repository.LectureRepository;
import com.edutrack.domain.lecture.repository.LectureStudentRepository;
import com.edutrack.domain.statistics.dto.StudentLectureAttendanceResponse;
import com.edutrack.domain.user.repository.UserRepository;
import com.edutrack.global.exception.ForbiddenException;
import com.edutrack.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 학생 출석 서비스
 * 현재 코드베이스의 복수 요일(daysOfWeek) 지원 기능과 호환되도록 작성되었습니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAttendanceService {

    private final StudentAttendanceRepository attendanceRepository;
    private final LectureStudentRepository lectureStudentRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    /**
     * 학생의 특정 강의 월별 출석 현황 조회
     * 
     * @param studentId 학생 ID
     * @param lectureId 강의 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 학생의 월별 출석 현황 정보
     */
    public StudentLectureAttendanceResponse getMonthlyAttendance(
            Long studentId, Long lectureId, int year, int month) {

        // 학생 존재 여부 확인
        validateStudentExists(studentId);

        // 학생이 해당 강의에 등록되어 있는지 확인
        com.edutrack.domain.lecture.entity.LectureStudentId lectureStudentId = 
                new com.edutrack.domain.lecture.entity.LectureStudentId(lectureId, studentId);
        if (!lectureStudentRepository.existsById(lectureStudentId)) {
            throw new ForbiddenException("해당 강의에 등록된 학생이 아닙니다.");
        }

        // 강의 정보 조회
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));

        // 해당 월의 첫날과 마지막날
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 강의 스케줄 기반으로 해당기간의 수업날짜 계산 (복수 요일 지원)
        List<LocalDate> classDates = calculateClassDates(lecture, startDate, endDate);
        int totalClassDays = classDates.size();

        // 학생의 출석한 날짜 목록 조회
        List<LocalDate> attendedDates = getAttendedDates(studentId, lecture.getDaysOfWeek(), startDate, endDate);
        int attendedDays = attendedDates.size();

        // 출석률 계산
        double myAttendanceRate = calculateStudentAttendanceRate(
                studentId, lecture.getDaysOfWeek(), startDate, endDate, totalClassDays);

        // 다른 수강생 평균 출석률 계산
        double otherStudentsAvgRate = calculateOtherStudentsAvgAttendanceRate(
                lectureId, studentId, lecture.getDaysOfWeek(), startDate, endDate, totalClassDays);

        return StudentLectureAttendanceResponse.builder()
                .lectureId(lectureId)
                .lectureName(lecture.getTitle())
                .year(year)
                .month(month)
                .attendedDates(attendedDates)
                .totalClassDays(totalClassDays)
                .attendedDays(attendedDays)
                .attendanceRate(myAttendanceRate)
                .otherStudentsAvgAttendanceRate(otherStudentsAvgRate)
                .build();
    }

    /**
     * 특정 학생의 출석한 날짜 목록 조회 (복수 요일 지원)
     */
    private List<LocalDate> getAttendedDates(Long studentId, List<DayOfWeek> lectureDays,
                                             LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(studentId, startDate, endDate);

        return attendances.stream()
                .map(Attendance::getDate)
                .filter(date -> lectureDays.contains(date.getDayOfWeek()))
                .collect(Collectors.toList());
    }

    /**
     * 출석률 계산 (복수 요일 지원)
     */
    private double calculateStudentAttendanceRate(Long studentId, List<DayOfWeek> lectureDays,
                                                  LocalDate startDate, LocalDate endDate, int totalClassDays) {
        if (totalClassDays == 0) return 0.0;

        List<LocalDate> attendedDates = getAttendedDates(studentId, lectureDays, startDate, endDate);
        return attendedDates.size() * 100.0 / totalClassDays;
    }

    /**
     * 다른 수강생 평균 출석률 계산 (복수 요일 지원)
     */
    private double calculateOtherStudentsAvgAttendanceRate(Long lectureId, Long excludeStudentId,
                                                          List<DayOfWeek> lectureDays, LocalDate startDate,
                                                          LocalDate endDate, int totalClassDays) {
        if (totalClassDays == 0) return 0.0;

        // 해당 강의의 모든 수강생 조회 (본인 제외)
        List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);

        List<Long> otherStudentIds = lectureStudents.stream()
                .map(ls -> ls.getStudent().getId())
                .filter(id -> !id.equals(excludeStudentId))
                .collect(Collectors.toList());

        if (otherStudentIds.isEmpty()) return 0.0;

        // 각 학생의 출석률 계산 후 평균
        double totalRate = 0.0;
        for (Long otherId : otherStudentIds) {
            totalRate += calculateStudentAttendanceRate(otherId, lectureDays, startDate, endDate, totalClassDays);
        }

        return totalRate / otherStudentIds.size();
    }

    /**
     * 강의 스케줄 기반으로 해당기간의 수업날짜 계산 (복수 요일 지원)
     */
    private List<LocalDate> calculateClassDates(Lecture lecture, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> classDates = new ArrayList<>();
        List<DayOfWeek> lectureDays = lecture.getDaysOfWeek();

        LocalDate lectureStart = lecture.getStartDate().toLocalDate();
        LocalDate lectureEnd = lecture.getEndDate().toLocalDate();

        LocalDate effectiveStart = startDate.isAfter(lectureStart) ? startDate : lectureStart;
        LocalDate effectiveEnd = endDate.isBefore(lectureEnd) ? endDate : lectureEnd;

        LocalDate current = effectiveStart;
        while (!current.isAfter(effectiveEnd)) {
            if (lectureDays.contains(current.getDayOfWeek())) {
                classDates.add(current);
            }
            current = current.plusDays(1);
        }

        return classDates;
    }

    private void validateStudentExists(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("학생을 찾을 수 없습니다. ID: " + studentId);
        }
    }
}
