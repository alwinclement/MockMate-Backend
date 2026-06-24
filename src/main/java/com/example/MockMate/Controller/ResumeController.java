package com.example.MockMate.Controller;

import com.example.MockMate.DTO.ResumeResponseDto;
import com.example.MockMate.Model.ResumeModel;
import com.example.MockMate.Service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponseDto> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        ResumeModel resume = resumeService.uploadAndParse(file, userDetails.getUsername());
        return ResponseEntity.ok(toDto(resume));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResumeResponseDto>> myResumes(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ResumeResponseDto> list = resumeService
                .getUserResumes(userDetails.getUsername())
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    private ResumeResponseDto toDto(ResumeModel r) {
        return new ResumeResponseDto(
                r.getId(), r.getFileName(), r.getCandidateName(),
                r.getEmail(), r.getPhone(), r.getSkills(),
                r.getExperience(), r.getAiSummary(), r.getUploadedAt()
        );
    }
}