package com.example.MockMate.Service;

import com.example.MockMate.DTO.ReportDto;
import com.example.MockMate.DTO.ScoreHistoryEntryDto;
import com.example.MockMate.Model.*;
import com.example.MockMate.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepo reportRepository;
    private final InterviewSessionRepo sessionRepository;
    private final QuestionRepo questionRepository;
    private final AnswerRepo answerRepository;
    private final EvaluationService evaluationService;
    private final UserDetailsRepo userRepository;

    // Called automatically when a session completes (Step 7 wires this in)
    public void generateReportForSession(Long sessionId) {
        // Avoid double-generating if somehow called twice
        if (reportRepository.findBySessionId(sessionId).isPresent()) {
            return;
        }

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Question> questions = questionRepository.findBySessionIdOrderByOrderIndex(sessionId);
        List<Answer> answers = answerRepository.findBySessionId(sessionId);

        try {
            EvaluationService.EvaluationResult result = evaluationService.evaluateSession(
                    questions, answers, session.getJobRole().getDisplayName()
            );

            double overall = (result.confidenceScore() + result.depthScore()
                    + result.communicationScore() + result.gapsScore()) / 4.0;

            Report report = Report.builder()
                    .session(session)
                    .confidenceScore(result.confidenceScore())
                    .depthScore(result.depthScore())
                    .communicationScore(result.communicationScore())
                    .gapsScore(result.gapsScore())
                    .overallScore(Math.round(overall * 10) / 10.0)   // round to 1 decimal
                    .overallFeedback(result.overallFeedback())
                    .weakAreas(result.weakAreas())
                    .suggestedTopics(result.suggestedTopics())
                    .build();

            reportRepository.save(report);

        } catch (Exception e) {
            System.out.println("Report generation skipped (Groq unavailable): " + e.getMessage());
            // Save a minimal fallback report so the user isn't left with nothing
            Report fallback = Report.builder()
                    .session(session)
                    .confidenceScore(3)
                    .depthScore(3)
                    .communicationScore(3)
                    .gapsScore(3)
                    .overallScore(3.0)
                    .overallFeedback("Evaluation could not be generated automatically. Please review your answers manually.")
                    .weakAreas(List.of())
                    .suggestedTopics(List.of())
                    .build();
            reportRepository.save(fallback);
        }
    }

    public ReportDto getReportBySessionId(Long sessionId) {
        Report report = reportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Report not found for session " + sessionId));
        return toDto(report);
    }

    public List<ScoreHistoryEntryDto> getScoreHistory(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reportRepository.findByUserIdOrderByGeneratedAtAsc(user.getId())
                .stream()
                .map(r -> ScoreHistoryEntryDto.builder()
                        .sessionId(r.getSession().getId())
                        .date(r.getGeneratedAt())
                        .jobRoleDisplay(r.getSession().getJobRole().getDisplayName())
                        .confidenceScore(r.getConfidenceScore())
                        .depthScore(r.getDepthScore())
                        .communicationScore(r.getCommunicationScore())
                        .gapsScore(r.getGapsScore())
                        .overallScore(r.getOverallScore())
                        .build())
                .toList();
    }

    private ReportDto toDto(Report r) {
        return ReportDto.builder()
                .id(r.getId())
                .sessionId(r.getSession().getId())
                .jobRoleDisplay(r.getSession().getJobRole().getDisplayName())
                .confidenceScore(r.getConfidenceScore())
                .depthScore(r.getDepthScore())
                .communicationScore(r.getCommunicationScore())
                .gapsScore(r.getGapsScore())
                .overallScore(r.getOverallScore())
                .overallFeedback(r.getOverallFeedback())
                .weakAreas(r.getWeakAreas())
                .suggestedTopics(r.getSuggestedTopics())
                .generatedAt(r.getGeneratedAt())
                .build();
    }
}