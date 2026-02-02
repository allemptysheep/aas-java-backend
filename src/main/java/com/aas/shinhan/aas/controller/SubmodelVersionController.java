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
import com.aas.shinhan.aas.entity.SubmodelEntity;
import com.aas.shinhan.aas.service.AasStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Submodel 버전 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/aas/submodels")
public class SubmodelVersionController {

    private final AasStorageService aasStorageService;

    /**
     * 특정 AAS에 연결된 활성화된 Submodel 목록 조회
     * GET /aas/submodels?aasId={aasId}
     */
    @GetMapping
    public ResponseEntity<List<SubmodelEntity>> getSubmodelsByAasId(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String aasId) {

        if (aasId != null) {
            log.info("AAS에 연결된 Submodel 목록 조회: aasId={}", aasId);
            List<SubmodelEntity> submodels = aasStorageService.getActiveSubmodelsByAasId(aasId);
            return ResponseEntity.ok(submodels);
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * 특정 Submodel 조회 (활성화된 버전)
     * GET /aas/submodels/{submodelId}
     */
    @GetMapping("/{submodelId}")
    public ResponseEntity<SubmodelEntity> getActiveSubmodel(@PathVariable String submodelId) {
        log.info("Submodel 조회: submodelId={}", submodelId);
        try {
            SubmodelEntity submodel = aasStorageService.getActiveSubmodel(submodelId);
            return ResponseEntity.ok(submodel);
        } catch (IllegalArgumentException e) {
            log.warn("Submodel을 찾을 수 없음: {}", submodelId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 Submodel의 버전 목록 조회
     * GET /aas/submodels/{submodelId}/versions
     */
    @GetMapping("/{submodelId}/versions")
    public ResponseEntity<List<VersionInfo>> getSubmodelVersions(@PathVariable String submodelId) {
        log.info("Submodel 버전 목록 조회: submodelId={}", submodelId);
        List<VersionInfo> versions = aasStorageService.getSubmodelVersions(submodelId);
        return ResponseEntity.ok(versions);
    }

    /**
     * 특정 Submodel 버전 활성화
     * POST /aas/submodels/{submodelId}/versions/{version}/activate
     */
    @PostMapping("/{submodelId}/versions/{version}/activate")
    public ResponseEntity<SubmodelEntity> activateSubmodelVersion(
            @PathVariable String submodelId,
            @PathVariable Integer version) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Submodel 버전 활성화 요청: submodelId={}, version={}, by={}", submodelId, version, username);

        try {
            SubmodelEntity activated = aasStorageService.activateSubmodelVersion(submodelId, version, username);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            log.warn("버전 활성화 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
