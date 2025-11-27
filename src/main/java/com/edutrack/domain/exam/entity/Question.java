package com.edutrack.domain.exam.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //exam 엔티티 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 문제 내용

    @Column(nullable = false)
    private Integer answerNumber; // 정답 번호 (1, 2, 3, 4 등)

    @Column(nullable = false)
    private Integer score; // 배점

    @Column(name = "unit_id", nullable = false)
    private Long unitId; // 단원 ID (FK)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty; // 난이도 (Difficulty Enum 필요)

    // 보기 리스트 (Question : Choice = 1 : N)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices = new ArrayList<>();

    @Builder
    public Question(Exam exam, String content, Integer answerNumber, Integer score, Long unitId, Difficulty difficulty) {
        this.exam = exam;
        this.content = content;
        this.answerNumber = answerNumber;
        this.score = score;
        this.unitId = unitId;
        this.difficulty = difficulty;
    }

    public void addChoice(Choice choice) {
        this.choices.add(choice);
        choice.setQuestion(this);
    }
}
