package com.example.MockMate.Repository;

import com.example.MockMate.Model.ResumeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepo extends JpaRepository<ResumeModel, Long> {
    List<ResumeModel> findByUserId(Long userId);
}