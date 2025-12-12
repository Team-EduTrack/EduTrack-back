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

    @Column(length = 100, nullable = false)
    private String title;

    @Column(name = "total_score")
    private Integer totalScore;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status;

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

    public Exam(Lecture lecture, String title, Integer totalScore, ExamStatus status, LocalDateTime startDate, LocalDateTime endDate, Integer durationMinute) {
        this.lecture = lecture;
        this.title = title;
        this.totalScore = totalScore;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationMinute = durationMinute;
        this.createdAt = LocalDateTime.now();
    }

    // === 상태 전환 메서드 ===

    /**
     * 시험을 공개 상태로 전환 (DRAFT → PUBLISHED)
     * 학생들이 응시 가능한 상태가 됨
     */
    public void publish() {
        if (this.status != ExamStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태의 시험만 공개할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ExamStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시험을 종료 상태로 전환 (PUBLISHED → CLOSED)
     * 더 이상 학생들이 응시/제출할 수 없음
     */
    public void close() {
        if (this.status != ExamStatus.PUBLISHED) {
            throw new IllegalStateException("PUBLISHED 상태의 시험만 종료할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = ExamStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시험이 응시 가능한 상태인지 확인
     */
    public boolean isAvailableForSubmission() {
        return this.status == ExamStatus.PUBLISHED;
    }

    /**
     * 시험이 종료되었는지 확인
     */
    public boolean isClosed() {
        return this.status == ExamStatus.CLOSED;
    }
}