package com.edutrack.domain.lecture.entity;
import com.edutrack.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "lecture_student")
public class LectureStudent {

    @EmbeddedId
    private LectureStudentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lectureId")
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User student;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "lecture_expire_date")
    private LocalDateTime expireDate;

    @Builder
    public LectureStudent(Lecture lecture, User student) {
        this.id = new LectureStudentId(lecture.getId(), student.getId());
        this.lecture = lecture;
        this.student = student;
        this.createdAt = LocalDateTime.now();
    }
}
