package com.edutrack.domain.exam.repository;

import com.edutrack.domain.exam.entity.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학생 답안 Repository
 */
@Repository
public interface ExamStudentAnswerRepository extends JpaRepository<ExamStudentAnswer, Long> {

    /**
     * 학생의 특정 시험에 대한 모든 답안 조회
     */
    List<ExamStudentAnswer> findAllByExamStudent(ExamStudent examStudent);

    /**
     * 학생의 특정 문제들에 대한 답안 조회
     */
    List<ExamStudentAnswer> findAllByExamStudentAndQuestionIn(
            ExamStudent examStudent,
            List<Question> questions
    );

    /**
     * 학생의 특정 문제에 대한 답안 조회
     */
    Optional<ExamStudentAnswer> findByExamStudentAndQuestion(
            ExamStudent examStudent,
            Question question
    );

    /**
     * 시험 ID와 학생 ID로 답안 목록 조회 (문제 정보 포함)
     */
    @Query("SELECT esa FROM ExamStudentAnswer esa " +
           "JOIN FETCH esa.question q " +
           "WHERE esa.examStudent.exam.id = :examId " +
           "AND esa.examStudent.student.id = :studentId " +
           "ORDER BY q.id")
    List<ExamStudentAnswer> findAllByExamIdAndStudentIdWithQuestion(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId
    );

    /**
     * 학생의 특정 시험 답안 개수 조회
     */
    @Query("SELECT COUNT(esa) FROM ExamStudentAnswer esa " +
           "WHERE esa.examStudent.exam.id = :examId " +
           "AND esa.examStudent.student.id = :studentId")
    int countByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
}
