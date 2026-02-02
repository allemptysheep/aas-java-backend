package com.aas.shinhan.aas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aas.shinhan.aas.dto.AasUploadResponse;
import com.aas.shinhan.aas.service.AasParserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aas")
public class AasUploadController {

    private final AasParserService aasParserService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAasFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ignore-duplicates", defaultValue = "false") boolean ignoreDuplicates) {

        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("=== AAS File Upload Start ===");
        log.info("Uploaded by: {}", username);
        log.info("Filename: {}", file.getOriginalFilename());
        log.info("File size: {} bytes ({} MB)", file.getSize(), file.getSize() / (1024 * 1024));
        log.info("Ignore duplicates: {}", ignoreDuplicates);

        try {
            AasUploadResponse response = aasParserService.parseAndProcess(file, username, ignoreDuplicates);
            log.info("=== AAS File Upload Complete ===");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("Failed to parse AAS file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to parse file: " + e.getMessage());
        }
    }
}
