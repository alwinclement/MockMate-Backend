package com.example.MockMate.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponseDto(
        Long id,
        String fileName,
        String candidateName,
        String email,
        String phone,
        List<String> skills,
        List<String> experience,
        String aiSummary,
        LocalDateTime uploadedAt
) {}
