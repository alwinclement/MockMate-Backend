package com.example.MockMate.Controller;

import com.example.MockMate.DTO.InterviewRequestDto;
import com.example.MockMate.DTO.InterviewSessionDto;
import com.example.MockMate.Service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews") // ← matches interviewSlice URL exactly
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    // POST /api/interviews/start
    // Body: { "resumeId": 1, "jobRole": "BACKEND_ENGINEER" }
    @PostMapping("/start")
    public ResponseEntity<InterviewSessionDto> startInterview(
            @RequestBody InterviewRequestDto request,  // ← body not params
            @AuthenticationPrincipal UserDetails userDetails) {

        InterviewSessionDto session = interviewService.startInterview(
                request.getResumeId(),
                request.getJobRole(),
                userDetails.getUsername()
        );
        return ResponseEntity.ok(session);
    }

    // GET /api/interviews/my
    @GetMapping("/my")
    public ResponseEntity<List<InterviewSessionDto>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                interviewService.getUserSessions(userDetails.getUsername())
        );
    }
}