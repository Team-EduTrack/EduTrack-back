package com.edutrack.domain.attendance.entity;

import com.edutrack.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "attendance")
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "attendance_date", nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private boolean status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User student;

  // 상태 변경 메서드
  public void attend() {
    this.status = true;
  }

  //생성자 (확장은 출석 횟수? 정도 까지만 확장 예정)
  public Attendance(LocalDate date, User student) {
    this.date = date;
    this.student = student;
    this.status = false; // 기본값은 결석(false)
  }
}
