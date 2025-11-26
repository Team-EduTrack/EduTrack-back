package com.edutrack.domain.exam.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "choice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 보기 내용

    @Column(nullable = false)
    private Integer choiceNumber; // 보기의 순서/번호 (1, 2, 3, 4)

    @Builder
    public Choice(Question question, String content, Integer choiceNumber) {
        this.question = question;
        this.content = content;
        this.choiceNumber = choiceNumber;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
