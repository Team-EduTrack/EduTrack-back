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
@Table(name = "user_email_verification")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEmailVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 이메일 직접 저장 (TempUser, User 둘 다 가능)
  @Column(name = "email", length = 100, nullable = false)
  private String email;

  @Column(name = "token", length = 255, nullable = false)
  private String token;

  @Builder.Default
  @Column(name = "is_verified", nullable = false)
  private boolean verified = false;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public void markVerified(){
    this.verified = true;
  }

}
