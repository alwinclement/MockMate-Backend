package com.example.MockMate.Repository;

import com.example.MockMate.Model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReportRepo extends JpaRepository<Report, Long> {
    Optional<Report> findBySessionId(Long sessionId);

    // For score history — ordered oldest to newest so charts plot left to right
    @org.springframework.data.jpa.repository.Query(
            "SELECT r FROM Report r WHERE r.session.user.id = :userId ORDER BY r.generatedAt ASC"
    )
    List<Report> findByUserIdOrderByGeneratedAtAsc(Long userId);
}