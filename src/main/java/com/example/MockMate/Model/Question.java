package com.example.MockMate.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;       // HR, TECHNICAL, CODING

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer orderIndex;      // 1-10, preserves question order

    @Column(columnDefinition = "TEXT")
    private String hint;             // optional AI-generated hint

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}