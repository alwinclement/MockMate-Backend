// InterviewRequest.java
package com.example.MockMate.DTO;

import com.example.MockMate.Model.JobRole;
import com.example.MockMate.Model.Question.Difficulty;
import lombok.Data;

@Data
public class InterviewStartRequest {
    private Long resumeId;
    private JobRole jobRole;
    private Difficulty difficulty;
}