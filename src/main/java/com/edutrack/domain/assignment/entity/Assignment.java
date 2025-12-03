package com.edutrack.domain.assignment.entity;
import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter
@NoArgsConstructor
public class Assignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 강의 과제인지 찾기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 강사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ===== 생성 정적 메서드 =====
    public static Assignment create(
            Lecture lecture,
            User teacher,
            String title,
            String description,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("과제 시작일은 종료일보다 늦을 수 없습니다.");
        }

        Assignment assignment = new Assignment();
        assignment.lecture = lecture;
        assignment.teacher = teacher;
        assignment.title = title;
        assignment.description = description;
        assignment.startDate = startDate;
        assignment.endDate = endDate;
        assignment.createdAt = LocalDateTime.now();
        return assignment;
    }
}