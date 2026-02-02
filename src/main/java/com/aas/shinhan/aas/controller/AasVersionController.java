package com.aas.shinhan.aas.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aas.shinhan.aas.dto.VersionInfo;
import com.aas.shinhan.aas.entity.AasEntity;
import com.aas.shinhan.aas.service.AasStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AAS 버전 관리 API 컨트롤러
 * BaSyx 프론트엔드의 aasVersionApi를 참고하여 구현
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aas/shells")
public class AasVersionController {

    private final AasStorageService aasStorageService;

    /**
     * 모든 활성화된 AAS 목록 조회
     * GET /aas/shells
     */
    @GetMapping
    public ResponseEntity<List<AasEntity>> getAllActiveAas() {
        log.info("모든 활성화된 AAS 목록 조회");
        List<AasEntity> aasList = aasStorageService.getAllActiveAas();
        return ResponseEntity.ok(aasList);
    }

    /**
     * 특정 AAS 조회 (활성화된 버전)
     * GET /aas/shells/{aasId}
     */
    @GetMapping("/{aasId}")
    public ResponseEntity<AasEntity> getActiveAas(@PathVariable String aasId) {
        log.info("AAS 조회: aasId={}", aasId);
        try {
            AasEntity aas = aasStorageService.getActiveAas(aasId);
            return ResponseEntity.ok(aas);
        } catch (IllegalArgumentException e) {
            log.warn("AAS를 찾을 수 없음: {}", aasId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 AAS의 버전 목록 조회
     * GET /aas/shells/{aasId}/versions
     */
    @GetMapping("/{aasId}/versions")
    public ResponseEntity<List<VersionInfo>> getAasVersions(@PathVariable String aasId) {
        log.info("AAS 버전 목록 조회: aasId={}", aasId);
        List<VersionInfo> versions = aasStorageService.getAasVersions(aasId);
        return ResponseEntity.ok(versions);
    }

    /**
     * 특정 AAS 버전 활성화
     * POST /aas/shells/{aasId}/versions/{version}/activate
     */
    @PostMapping("/{aasId}/versions/{version}/activate")
    public ResponseEntity<AasEntity> activateAasVersion(
            @PathVariable String aasId,
            @PathVariable Integer version) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("AAS 버전 활성화 요청: aasId={}, version={}, by={}", aasId, version, username);

        try {
            AasEntity activated = aasStorageService.activateAasVersion(aasId, version, username);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            log.warn("버전 활성화 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
