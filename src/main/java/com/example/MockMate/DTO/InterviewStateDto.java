package com.example.MockMate.DTO;

import com.example.MockMate.Model.QuestionType;
import com.example.MockMate.Model.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStateDto {

    private Long sessionId;
    private String event;          // QUESTION, TIME_UP, COMPLETED, ERROR

    // Current question details
    private Long questionId;
    private String questionContent;
    private QuestionType questionType;
    private Question.Difficulty difficulty;
    private String hint;

    // Progress
    private int currentIndex;      // 0-based
    private int totalQuestions;
    private int timeRemainingSeconds;

    // On completion
    private int totalAnswered;
    private int timedOutCount;
}