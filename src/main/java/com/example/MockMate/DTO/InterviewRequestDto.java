package com.example.MockMate.DTO;

import com.example.MockMate.Model.JobRole;
import lombok.Data;

@Data
public class InterviewRequestDto {
    private Long resumeId;
    private JobRole jobRole;
    // No questionCount — always 10, decided by backend
}