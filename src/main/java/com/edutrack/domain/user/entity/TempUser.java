package com.edutrack.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TempUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "login_id", nullable = false, unique = true, length = 50)
  private String loginId;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "phone", nullable = false, unique = true, length = 11)
  private String phone;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  // 이메일 인증 이후에만 세팅됨
  @Column(name = "academy_code", length = 50)
  private String academyCode;

  @Builder.Default
  @Column(name = "verified", nullable = false)
  private boolean verified = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public void markVerified() {
    this.verified = true;
  }

  // 학원 코드 입력 단계에서 호출
  public void updateAcademyCode(String academyCode) {
    this.academyCode = academyCode;
  }

}
