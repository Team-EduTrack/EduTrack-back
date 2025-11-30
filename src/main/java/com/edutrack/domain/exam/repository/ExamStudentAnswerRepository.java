package com.edutrack.domain.exam.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.edutrack.domain.exam.ExamStudent;
import com.edutrack.domain.exam.entity.ExamStudentAnswer;
import com.edutrack.domain.exam.entity.Question;

import java.util.List;

public interface ExamStudentAnswerRepository extends JpaRepository<ExamStudentAnswer, Long> {

    List<ExamStudentAnswer> findAllByExamStudent(ExamStudent examStudent);
    List<ExamStudentAnswer> findAllByExamStudentAndQuestionIn(
            ExamStudent examStudent,
            List<Question> questions
    );
}
