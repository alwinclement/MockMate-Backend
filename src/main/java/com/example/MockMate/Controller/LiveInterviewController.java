package com.example.MockMate.Controller;

import com.example.MockMate.DTO.AnswerDto;
import com.example.MockMate.DTO.SubmitAnswerRequest;
import com.example.MockMate.Service.LiveInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LiveInterviewController {

    private final LiveInterviewService liveInterviewService;

    // HTTP endpoint — called once to start the live session
    @PostMapping("/api/interview/live/start/{sessionId}")
    @ResponseBody
    public ResponseEntity<String> startLive(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        liveInterviewService.startLiveSession(sessionId);
        return ResponseEntity.ok("Live session started");
    }

    // WebSocket message handler — client sends answer via STOMP
    @MessageMapping("/interview/answer")
    public void handleAnswer(@Payload SubmitAnswerRequest request) {
        liveInterviewService.submitAnswer(request);
    }

    // HTTP endpoint — fetch all answers after session ends
    @GetMapping("/api/interview/live/{sessionId}/answers")
    @ResponseBody
    public ResponseEntity<List<AnswerDto>> getAnswers(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(liveInterviewService.getSessionAnswers(sessionId));
    }
}