package com.example.MockMate.DTO;

import com.example.MockMate.Model.InterviewSession;
import com.example.MockMate.Model.JobRole;
import java.time.LocalDateTime;
import java.util.List;

public record InterviewSessionDto(
        Long id,
        JobRole jobRole,
        String jobRoleDisplay,
        InterviewSession.SessionStatus status,
        List<QuestionDto> questions,
        List<QuestionDto> hrQuestions,
        List<QuestionDto> technicalQuestions,
        List<QuestionDto> codingQuestions,
        LocalDateTime createdAt
) {}
