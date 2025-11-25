package com.edutrack.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureCreationResponse {
    private final Long lectureId;
}
