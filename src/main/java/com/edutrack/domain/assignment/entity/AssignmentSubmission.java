package com.edutrack.domain.assignment.entity;

import com.edutrack.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submission")
@Getter
@NoArgsConstructor
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignments_id", nullable = false)
    private Assignment assignment;

    // 제출한 학생
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Integer score;

    private String feedback;

    public AssignmentSubmission(Assignment assignment, User student, String filePath) {
        this.assignment = assignment;
        this.student = student;
        this.filePath = filePath;
        this.createdAt = LocalDateTime.now();
    }

    public void grade(int score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }
}