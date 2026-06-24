package com.example.MockMate.Controller;

import com.example.MockMate.DTO.ReportDto;
import com.example.MockMate.DTO.ScoreHistoryEntryDto;
import com.example.MockMate.Service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<ReportDto> getReport(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(reportService.getReportBySessionId(sessionId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ScoreHistoryEntryDto>> getScoreHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(reportService.getScoreHistory(userDetails.getUsername()));
    }
}