package com.edutrack.domain.principal.dto;

import com.edutrack.domain.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PrincipalLectureResponse {

    private final Long lectureId;
    private final String title;
    private final String teacherName;
    private final int studentCount;
    private final String imageUrl;

    public static PrincipalLectureResponse of(Lecture lecture, int studentCount) {
        return PrincipalLectureResponse.builder()
                .lectureId(lecture.getId())
                .title(lecture.getTitle())
                .teacherName(lecture.getTeacher() != null ? lecture.getTeacher().getName() : null)
                .studentCount(studentCount)
                .imageUrl(lecture.getImageUrl())
                .build();
    }
}

