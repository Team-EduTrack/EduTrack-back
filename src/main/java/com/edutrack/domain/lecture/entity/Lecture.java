package com.edutrack.domain.lecture.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.edutrack.domain.academy.Academy;
import com.edutrack.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lecture")
@EntityListeners(AuditingEntityListener.class)
public class Lecture {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "academy_id", referencedColumnName = "id",  nullable = false)
  private Academy academy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false)
  private User teacher;

  @Column(length = 100, nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Builder.Default
  @ElementCollection(fetch = FetchType.LAZY)
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false)
  @Builder.Default
  private List<DayOfWeek> daysOfWeek = new ArrayList<>();

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Lecture(Academy academy, User teacher, String title, String description, List<DayOfWeek> daysOfWeek,
      LocalDateTime startDate, LocalDateTime endDate) {
    this.academy = academy;
    this.teacher = teacher;
    this.title = title;
    this.description = description;
    this.daysOfWeek = daysOfWeek != null ? new ArrayList<>(daysOfWeek) : new ArrayList<>();
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @PrePersist
  protected void onCreate() {
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }
}