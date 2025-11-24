package com.edutrack.domain.exam.entity;

import com.edutrack.domain.lecture.entity.Lecture;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam")
@Getter
@NoArgsConstructor
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 강의 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 시험 응시 가능 기간
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // 시험 시간(분)
    @Column(name = "duration_minute")
    private Integer durationMinute;

    public Exam(Lecture lecture, LocalDateTime startDate, LocalDateTime endDate, Integer durationMinute) {
        this.lecture = lecture;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinute = durationMinute;
        this.createdAt = LocalDateTime.now();
    }
}