package com.edutrack.domain.lecture.lectureStudent.entity;

import com.edutrack.domain.lecture.entity.Lecture;
import com.edutrack.domain.user.User;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class LectureStudentId implements Serializable {

  @ManyToOne(fetch = FetchType.LAZY)
  private Lecture lecture;

  @ManyToOne(fetch = FetchType.LAZY)
  private User student;

  public LectureStudentId(Lecture lecture, User student) {
    this.lecture = lecture;
    this.student = student;
  }



}
