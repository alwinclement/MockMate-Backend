package com.example.MockMate.Service;

import com.example.MockMate.Model.ResumeModel;
import com.example.MockMate.Model.UserModel;
import com.example.MockMate.Repository.ResumeRepo;
import com.example.MockMate.Repository.UserDetailsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final PdfExtractorService pdfExtractorService;
    private final ResumeParserService parserService;
    private final AiEnrichmentService aiEnrichmentService;
    private final ResumeRepo resumeRepository;
    private final UserDetailsRepo userRepository;

    public ResumeModel uploadAndParse(MultipartFile file, String userEmail) throws IOException {

        // 1. Find user
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Extract raw text from PDF
        String rawText = pdfExtractorService.extractText(file);

        // 3. Regex parse
        String name     = parserService.extractName(rawText);
        String email    = parserService.extractEmail(rawText);
        String phone    = parserService.extractPhone(rawText);
        List<String> regexSkills     = parserService.extractSkills(rawText);
        List<String> regexExperience = parserService.extractExperience(rawText);

        // 4. Declare final variables BEFORE the try block
        Set<String> mergedSkills = new LinkedHashSet<>(regexSkills);
        List<String> finalExperience = regexExperience;
        String aiSummary = null;

        // 5. AI enrichment — fallback gracefully if Groq fails
        try {
            AiEnrichmentService.AiResumeResult aiResult =
                    aiEnrichmentService.enrich(rawText, regexSkills);
            mergedSkills.addAll(aiResult.skills());
            if (!aiResult.experience().isEmpty()) {
                finalExperience = aiResult.experience();
            }
            aiSummary = aiResult.summary();
        } catch (Exception e) {
            System.out.println("AI enrichment skipped (Groq unavailable): " + e.getMessage());
        }

        // 6. Save to DB — uses the variables declared above
        ResumeModel resume = ResumeModel.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .candidateName(name)
                .email(email)
                .phone(phone)
                .rawText(rawText)
                .skills(new ArrayList<>(mergedSkills))
                .experience(finalExperience)
                .aiSummary(aiSummary)
                .build();

        return resumeRepository.save(resume);
    }

    public List<ResumeModel> getUserResumes(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return resumeRepository.findByUserId(user.getId());
    }
}
