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
public class AnswerDto {
    private Long id;
    private Long questionId;
    private String questionContent;
    private String answerText;
    private boolean answeredByVoice;
    private int timeTakenSeconds;
    private boolean timedOut;
    private LocalDateTime answeredAt;
}