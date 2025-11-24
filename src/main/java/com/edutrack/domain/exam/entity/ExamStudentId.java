package com.edutrack.domain.exam.entity;

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
public class ExamStudentId implements Serializable {

    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "user_id")
    private Long userId;
}