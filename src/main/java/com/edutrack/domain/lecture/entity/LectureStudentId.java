package com.edutrack.domain.lecture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable

public class LectureStudentId implements Serializable {
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "user_id")
    private Long userId;
}
