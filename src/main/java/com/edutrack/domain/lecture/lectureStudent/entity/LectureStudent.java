package com.edutrack.domain.lecture.lectureStudent.entity;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.user.User;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lecture_student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureStudent {

  @EmbeddedId
  private LectureStudentId id;

  @MapsId("lecture")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lecture_id", referencedColumnName = "id", nullable = false)
  private Lecture lecture;

  @MapsId("student")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
  private User student;

  public LectureStudent(Lecture lecture, User student) {
    this.id = new LectureStudentId(lecture, student);
    this.lecture = lecture;
    this.student = student;
  }
}
