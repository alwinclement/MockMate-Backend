package com.example.MockMate.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private InterviewSession session;

    // 1-5 star ratings per category
    private Integer confidenceScore;
    private Integer depthScore;
    private Integer communicationScore;
    private Integer gapsScore;        // higher = fewer gaps, 5 = no gaps found

    // Overall average of the four scores, kept as a separate stored field
    // so the frontend and trend charts don't need to recompute it everywhere
    private Double overallScore;

    @Column(columnDefinition = "TEXT")
    private String overallFeedback;   // 2-3 sentence summary of performance

    @ElementCollection
    @CollectionTable(name = "report_weak_areas", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "weak_area", columnDefinition = "TEXT")
    private List<String> weakAreas;       // e.g. "System design depth", "STAR method structure"

    @ElementCollection
    @CollectionTable(name = "report_suggested_topics", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "topic", columnDefinition = "TEXT")
    private List<String> suggestedTopics; // e.g. "Practice explaining trade-offs in system design"

    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }
}