package com.example.MockMate.DTO;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private Long sessionId;
    private Long questionId;
    private String answerText;
    private boolean answeredByVoice;
    private int timeTakenSeconds;
    private boolean timedOut;
}