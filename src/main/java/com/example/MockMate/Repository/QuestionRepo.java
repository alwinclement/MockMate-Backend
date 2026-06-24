package com.example.MockMate.Repository;

import com.example.MockMate.Model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findBySessionIdOrderByOrderIndex(Long sessionId);
}
