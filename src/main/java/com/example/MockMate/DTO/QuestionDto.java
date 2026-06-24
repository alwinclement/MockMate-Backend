package com.example.MockMate.DTO;

import com.example.MockMate.Model.Question;
import com.example.MockMate.Model.QuestionType;

public record QuestionDto(
        Long id,
        String content,
        QuestionType type,
        Question.Difficulty difficulty,
        Integer orderIndex,
        String hint
) {}