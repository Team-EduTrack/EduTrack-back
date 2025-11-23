package com.edutrack.domain.academy;

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

@Entity
@Table(name = "academy")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Academy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // principal_user_id 는 나중에 연관관계 걸거나 Long 으로만 둬도 됨
  @Column(name = "principal_user_id")
  private Long principalUserId;

  @Column(name = "name", length = 100)
  private String name;

  // 학원코드 : EDU-1234 형식
  @Column(name = "code", length = 20, unique = true)
  private String code;

  @Column(name = "created_At")
  private LocalDateTime createdAt;

}
