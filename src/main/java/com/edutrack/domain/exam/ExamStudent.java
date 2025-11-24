package com.edutrack.domain.exam;

import com.edutrack.domain.exam.entity.Exam;
import com.edutrack.domain.exam.entity.ExamStudentId;
import com.edutrack.domain.user.User;
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

    @Column(name = "status")
    private String status;

    @Column(name="exam_started_at")
    private LocalDateTime startedAt;

    public ExamStudent(Exam exam, User student) {
        this.id = new ExamStudentId(exam.getId(), student.getId());
        this.exam = exam;
        this.student = student;
        this.startedAt = LocalDateTime.now();
        this.status = "IN_PROGRESS";
    }

    public void complete(int earnedScore) {
        this.earnedScore = earnedScore;
        this.status = "COMPLETED";
    }
}
