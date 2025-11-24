package com.edutrack.api.lecture.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureCreationResponse {
    private final Long lectureId;
}
