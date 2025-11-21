package com.edutrack.domain.academy;

import com.edutrack.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "academy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Academy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principal_user_id", referencedColumnName = "id", nullable = false)
    private User principalUser;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20, unique = true, nullable = false)
    private String code;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Academy(String name, String code, User principalUser) {
        this.name = name;
        this.code = code;
        this.principalUser = principalUser;
    }
}
