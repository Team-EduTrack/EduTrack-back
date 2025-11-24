package com.edutrack.domain.user.entity;

import com.edutrack.domain.academy.Academy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // fk : academy_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "academy_id")
  private Academy academy;

  @Column(name = "login_id", length = 50, nullable = false, unique = true)
  private String loginId;

  @Column(name = "password", length = 255, nullable = false)
  private String password;

  @Column(name = "name", length = 100, nullable = false, unique = true)
  private String name;

  @Column(name = "phone", length = 11, nullable = false, unique = true)
  private String phone;

  @Column(name = "email", length = 100, nullable = false, unique = true)
  private String email;

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_status", nullable = false, length = 20)

  private UserStatus userStatus;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_to_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public User(String loginId, String password, String name, String phone, String email, Academy academy) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.academy = academy;
        this.emailVerified = false;
        this.userStatus = UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasRole(RoleType roleType) {
        return roles.stream().anyMatch(role -> role.getName() == roleType);
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }
    public void addRole(Role role) {
        roles.add(role);
    }

    public void setAcademy(Academy academy) {
        this.academy = academy;
    }

}
