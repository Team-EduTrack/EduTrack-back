package com.edutrack.domain.exam.entity;

import com.edutrack.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_student")
@Getter
@NoArgsConstructor
public class ExamStudent {

    @EmbeddedId
    private ExamStudentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examId")
    @JoinColumn(name="exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name="user_id")
    private User student;

    @Column(name = "earned_score")
    private Integer earnedScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StudentExamStatus status;

    @Column(name="exam_started_at")
    private LocalDateTime startedAt;

    @Column(name="submitted_at")
    private LocalDateTime submittedAt;

    public ExamStudent(Exam exam, User student) {
        this.id = new ExamStudentId(exam.getId(), student.getId());
        this.exam = exam;
        this.student = student;
        this.startedAt = LocalDateTime.now();
        this.status = StudentExamStatus.IN_PROGRESS;
    }

    public void submit() {
        this.status = StudentExamStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void complete(int earnedScore) {
        this.earnedScore = earnedScore;
        this.status = StudentExamStatus.GRADED;
    }

    public boolean isSubmitted() {
        return this.status == StudentExamStatus.SUBMITTED || this.status == StudentExamStatus.GRADED;
    }
}
