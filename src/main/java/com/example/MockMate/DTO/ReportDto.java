package com.example.MockMate.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private Long sessionId;
    private String jobRoleDisplay;

    private Integer confidenceScore;
    private Integer depthScore;
    private Integer communicationScore;
    private Integer gapsScore;
    private Double overallScore;

    private String overallFeedback;
    private List<String> weakAreas;
    private List<String> suggestedTopics;

    private LocalDateTime generatedAt;
}