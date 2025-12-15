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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAttendanceService {

    private final StudentAttendanceRepository attendanceRepository;
    private final LectureStudentRepository lectureStudentRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;


    //학생의 특정 강의 월별 출석 현황 조회
    public StudentLectureAttendanceResponse getMonthlyAttendance(
            Long studentId, Long lectureId, int year, int month, Long principalId) {

        // 권한 검증
        validateOwnership(studentId, principalId);

        validateStudentExists(studentId);


        if (!lectureStudentRepository.existsByLecture_IdAndStudent_Id(lectureId, studentId)) {
            throw new NotFoundException("해당 강의에 등록된 학생이 아닙니다.");
        }


        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다. ID: " + lectureId));


        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());


        List<LocalDate> classDates = calculateClassDates(lecture, startDate, endDate);
        int totalClassDays = classDates.size();


        double myAttendanceRate = calculateStudentAttendanceRate(
                studentId, lecture.getDayOfWeek(), startDate, endDate, totalClassDays);


        List<LocalDate> attendedDates = getAttendedDates(studentId, lecture.getDayOfWeek(), startDate, endDate);
        int attendedDays = attendedDates.size();


        double otherStudentsAvgRate = calculateOtherStudentsAvgAttendanceRate(
                lectureId, studentId, lecture.getDayOfWeek(), startDate, endDate, totalClassDays);

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


    //특정 학생의 출석한 날짜 목록 조회
    private List<LocalDate> getAttendedDates(Long studentId, DayOfWeek lectureDay,
                                             LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByStudentIdAndDateBetweenAndStatusTrueOrderByDateAsc(studentId, startDate, endDate);

        return attendances.stream()
                .map(Attendance::getDate)
                .filter(date -> date.getDayOfWeek() == lectureDay)
                .collect(Collectors.toList());
    }

  //출석률 계산
    private double calculateStudentAttendanceRate(Long studentId, DayOfWeek lectureDay,
                                                  LocalDate startDate, LocalDate endDate, int totalClassDays) {
        if (totalClassDays == 0) return 0.0;

        List<LocalDate> attendedDates = getAttendedDates(studentId, lectureDay, startDate, endDate);
        return attendedDates.size() * 100.0 / totalClassDays;
    }

    //다른 수강생 평균 출석률 계산 (배치 조회)
    private double calculateOtherStudentsAvgAttendanceRate(Long lectureId, Long excludeStudentId,
                                                           DayOfWeek lectureDay, LocalDate startDate,
                                                           LocalDate endDate, int totalClassDays) {
        if (totalClassDays == 0) return 0.0;

        // 해당 강의의 모든 수강생 조회 (본인 제외)
        List<LectureStudent> lectureStudents = lectureStudentRepository.findAllByLectureId(lectureId);

        List<Long> otherStudentIds = lectureStudents.stream()
                .map(ls -> ls.getStudent().getId())
                .filter(id -> !id.equals(excludeStudentId))
                .collect(Collectors.toList());

        if (otherStudentIds.isEmpty()) return 0.0;

        // 배치로 모든 학생의 출석 기록 한 번에 조회
        List<Attendance> allAttendances = attendanceRepository
                .findByStudentIdInAndDateBetweenAndStatusTrueOrderByDateAsc(otherStudentIds, startDate, endDate);

        // 학생별로 그룹핑하여 출석률 계산
        Map<Long, List<Attendance>> attendanceByStudent = allAttendances.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        double totalRate = 0.0;
        for (Long otherId : otherStudentIds) {
            List<Attendance> studentAttendances = attendanceByStudent.getOrDefault(otherId, Collections.emptyList());
            long attendedDays = studentAttendances.stream()
                    .filter(a -> a.getDate().getDayOfWeek() == lectureDay)
                    .count();
            totalRate += (attendedDays * 100.0 / totalClassDays);
        }

        return totalRate / otherStudentIds.size();
    }


    //강의 스케줄 기반으로 해당기간의 수업날짜 계산
    private List<LocalDate> calculateClassDates(Lecture lecture, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> classDates = new ArrayList<>();
        DayOfWeek lectureDay = lecture.getDayOfWeek();

        LocalDate lectureStart = lecture.getStartDate().toLocalDate();
        LocalDate lectureEnd = lecture.getEndDate().toLocalDate();

        LocalDate effectiveStart = startDate.isAfter(lectureStart) ? startDate : lectureStart;
        LocalDate effectiveEnd = endDate.isBefore(lectureEnd) ? endDate : lectureEnd;

        LocalDate current = effectiveStart;
        while (!current.isAfter(effectiveEnd)) {
            if (current.getDayOfWeek() == lectureDay) {
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

    private void validateOwnership(Long studentId, Long principalId) {
        if (!studentId.equals(principalId)) {
            throw new ForbiddenException("본인의 출석 현황만 조회할 수 있습니다.");
        }
    }
}