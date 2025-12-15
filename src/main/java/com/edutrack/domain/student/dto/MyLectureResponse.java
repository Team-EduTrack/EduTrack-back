package com.edutrack.domain.student.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 내 강의 목록 응답 DTO
 * - JPQL DTO Projection용 클래스
 */
@Getter
@Builder
public class MyLectureResponse {
    
    private final Long lectureId;
    private final String lectureTitle;
    private final String teacherName;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    /**
     * JPQL new 연산자용 생성자
     */
    public MyLectureResponse(Long lectureId, String lectureTitle, 
                             String teacherName, LocalDateTime startDate, 
                             LocalDateTime endDate) {
        this.lectureId = lectureId;
        this.lectureTitle = lectureTitle;
        this.teacherName = teacherName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
