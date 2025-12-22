package com.edutrack.domain.student.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

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
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String imageUrl;

    /**
     * JPQL new 연산자용 생성자
     */
    public MyLectureResponse(Long lectureId, String lectureTitle, 
                             String teacherName, String description, LocalDateTime startDate,
                             LocalDateTime endDate, String imageUrl) {
        this.lectureId = lectureId;
        this.lectureTitle = lectureTitle;
        this.teacherName = teacherName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
    }
}
