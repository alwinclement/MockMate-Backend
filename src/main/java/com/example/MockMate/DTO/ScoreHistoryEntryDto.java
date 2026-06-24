package com.example.MockMate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistoryEntryDto {
    private Long sessionId;
    private LocalDateTime date;
    private String jobRoleDisplay;
    private Integer confidenceScore;
    private Integer depthScore;
    private Integer communicationScore;
    private Integer gapsScore;
    private Double overallScore;
}