package com.edutrack.domain.exam.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_student_answer")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamStudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // exam_student에 대한 답인지 체크 ( 복합키 기반 )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "exam_id", referencedColumnName = "exam_id"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private ExamStudent examStudent;

    // 문제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    //단원
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    //난이도
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;

    // 학생이 선택한 답(answerNumber)
    @Column(name = "submitted_answer_number", nullable = false)
    private Integer submittedAnswerNumber;

    // 정답 여부
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    // 획득 점수
    @Column(name = "earned_score")
    private Integer earnedScore;

    // 팩토리 메서드
    public static ExamStudentAnswer create(
            ExamStudent examStudent,
            Question question,
            Integer submittedAnswerNumber
    ){
      return ExamStudentAnswer.builder()
              .examStudent(examStudent)
              .question(question)
              .unitId(question.getUnitId())
              .difficulty(question.getDifficulty())
              .submittedAnswerNumber(submittedAnswerNumber)
              .correct(false) // 초기값 false
              .earnedScore(0) // 초기값 0
              .build();
    }

    // 채점
    public void mark(boolean correct, int earnedScore) {
        this.correct = correct;
        this.earnedScore = earnedScore;
    }
}
