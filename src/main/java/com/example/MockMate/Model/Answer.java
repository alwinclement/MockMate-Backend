package com.example.MockMate.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    private Boolean answeredByVoice;

    // How long the candidate took in seconds
    private Integer timeTakenSeconds;

    // Whether time ran out before they answered
    private Boolean timedOut;

    private LocalDateTime answeredAt;

    @PrePersist
    public void prePersist() {
        this.answeredAt = LocalDateTime.now();
    }
}