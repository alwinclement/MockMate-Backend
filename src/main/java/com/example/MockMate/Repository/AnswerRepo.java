package com.example.MockMate.Repository;

import com.example.MockMate.Model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnswerRepo extends JpaRepository<Answer, Long> {
    List<Answer> findBySessionId(Long sessionId);
    Optional<Answer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);
    long countBySessionId(Long sessionId);
}