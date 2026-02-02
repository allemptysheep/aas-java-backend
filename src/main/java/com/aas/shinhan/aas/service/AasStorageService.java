package com.aas.shinhan.aas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aas.shinhan.aas.dto.AasSaveRequest;
import com.aas.shinhan.aas.dto.SubmodelSaveRequest;
import com.aas.shinhan.aas.dto.VersionInfo;
import com.aas.shinhan.aas.entity.AasEntity;
import com.aas.shinhan.aas.entity.ConceptDescriptionEntity;
import com.aas.shinhan.aas.entity.SubmodelEntity;
import com.aas.shinhan.aas.repository.AasRepository;
import com.aas.shinhan.aas.repository.ConceptDescriptionRepository;
import com.aas.shinhan.aas.repository.SubmodelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AAS 저장 및 버전 관리 서비스
 * BaSyx의 Repository 패턴과 IdentifiableUploader 패턴을 참고하여 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AasStorageService {

    private final AasRepository aasRepository;
    private final SubmodelRepository submodelRepository;
    private final ConceptDescriptionRepository conceptDescriptionRepository;

    // ======================= AAS 관련 메서드 =======================

    /**
     * AAS 저장 (새로 생성 또는 새 버전 생성)
     */
    @Transactional
    public AasEntity saveAas(AasSaveRequest request, String username) {
        String aasId = request.getAasId();

        // 기존 버전 존재 여부 확인
        Integer maxVersion = aasRepository.findMaxVersionByAasId(aasId).orElse(0);

        if (maxVersion > 0 && request.isCreateNewVersion()) {
            // 새 버전 생성: 기존 모든 버전 비활성화
            aasRepository.deactivateAllVersionsByAasId(aasId);
            log.info("AAS [{}] 기존 버전들 비활성화 완료", aasId);
        }

        int newVersion = request.isCreateNewVersion() || maxVersion == 0 ? maxVersion + 1 : maxVersion;

        AasEntity entity = AasEntity.builder()
                .aasId(aasId)
                .idShort(request.getIdShort())
                .assetKind(request.getAssetKind())
                .globalAssetId(request.getGlobalAssetId())
                .aasJson(request.getAasJson())
                .version(newVersion)
                .isActive(true)
                .createdBy(username)
                .build();

        AasEntity saved = aasRepository.save(entity);
        log.info("AAS 저장 완료: id={}, version={}, createdBy={}", aasId, newVersion, username);

        return saved;
    }

    /**
     * AAS 버전 목록 조회
     */
    @Transactional(readOnly = true)
    public List<VersionInfo> getAasVersions(String aasId) {
        return aasRepository.findByAasIdOrderByVersionDesc(aasId).stream()
                .map(this::toVersionInfo)
                .collect(Collectors.toList());
    }

    /**
     * AAS 특정 버전 활성화
     */
    @Transactional
    public AasEntity activateAasVersion(String aasId, Integer version, String username) {
        // 기존 활성화된 버전 비활성화
        aasRepository.deactivateAllVersionsByAasId(aasId);

        // 특정 버전 활성화
        AasEntity entity = aasRepository.findByAasIdAndVersion(aasId, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "AAS not found: aasId=" + aasId + ", version=" + version));

        entity.setIsActive(true);
        entity.setUpdatedBy(username);

        log.info("AAS 버전 활성화: id={}, version={}, activatedBy={}", aasId, version, username);
        return aasRepository.save(entity);
    }

    /**
     * 활성화된 AAS 조회
     */
    @Transactional(readOnly = true)
    public AasEntity getActiveAas(String aasId) {
        return aasRepository.findByAasIdAndIsActiveTrue(aasId)
                .orElseThrow(() -> new IllegalArgumentException("Active AAS not found: " + aasId));
    }

    /**
     * 모든 활성화된 AAS 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AasEntity> getAllActiveAas() {
        return aasRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    // ======================= Submodel 관련 메서드 =======================

    /**
     * Submodel 저장 (새로 생성 또는 새 버전 생성)
     */
    @Transactional
    public SubmodelEntity saveSubmodel(SubmodelSaveRequest request, String username) {
        String submodelId = request.getSubmodelId();

        // 기존 버전 존재 여부 확인
        Integer maxVersion = submodelRepository.findMaxVersionBySubmodelId(submodelId).orElse(0);

        if (maxVersion > 0 && request.isCreateNewVersion()) {
            // 새 버전 생성: 기존 모든 버전 비활성화
            submodelRepository.deactivateAllVersionsBySubmodelId(submodelId);
            log.info("Submodel [{}] 기존 버전들 비활성화 완료", submodelId);
        }

        int newVersion = request.isCreateNewVersion() || maxVersion == 0 ? maxVersion + 1 : maxVersion;

        SubmodelEntity entity = SubmodelEntity.builder()
                .submodelId(submodelId)
                .idShort(request.getIdShort())
                .semanticId(request.getSemanticId())
                .aasId(request.getAasId())
                .submodelJson(request.getSubmodelJson())
                .version(newVersion)
                .isActive(true)
                .createdBy(username)
                .build();

        SubmodelEntity saved = submodelRepository.save(entity);
        log.info("Submodel 저장 완료: id={}, version={}, createdBy={}", submodelId, newVersion, username);

        return saved;
    }

    /**
     * Submodel 버전 목록 조회
     */
    @Transactional(readOnly = true)
    public List<VersionInfo> getSubmodelVersions(String submodelId) {
        return submodelRepository.findBySubmodelIdOrderByVersionDesc(submodelId).stream()
                .map(this::toVersionInfo)
                .collect(Collectors.toList());
    }

    /**
     * Submodel 특정 버전 활성화
     */
    @Transactional
    public SubmodelEntity activateSubmodelVersion(String submodelId, Integer version, String username) {
        // 기존 활성화된 버전 비활성화
        submodelRepository.deactivateAllVersionsBySubmodelId(submodelId);

        // 특정 버전 활성화
        SubmodelEntity entity = submodelRepository.findBySubmodelIdAndVersion(submodelId, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Submodel not found: submodelId=" + submodelId + ", version=" + version));

        entity.setIsActive(true);
        entity.setUpdatedBy(username);

        log.info("Submodel 버전 활성화: id={}, version={}, activatedBy={}", submodelId, version, username);
        return submodelRepository.save(entity);
    }

    /**
     * 특정 AAS에 연결된 활성화된 Submodel 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SubmodelEntity> getActiveSubmodelsByAasId(String aasId) {
        return submodelRepository.findByAasIdAndIsActiveTrueOrderByIdShort(aasId);
    }

    /**
     * 활성화된 Submodel 조회
     */
    @Transactional(readOnly = true)
    public SubmodelEntity getActiveSubmodel(String submodelId) {
        return submodelRepository.findBySubmodelIdAndIsActiveTrue(submodelId)
                .orElseThrow(() -> new IllegalArgumentException("Active Submodel not found: " + submodelId));
    }

    // ======================= ConceptDescription 관련 메서드 =======================

    /**
     * ConceptDescription 저장
     */
    @Transactional
    public ConceptDescriptionEntity saveConceptDescription(String cdId, String idShort,
            String cdJson, boolean createNewVersion, String username) {

        Integer maxVersion = conceptDescriptionRepository.findMaxVersionByCdId(cdId).orElse(0);

        if (maxVersion > 0 && createNewVersion) {
            conceptDescriptionRepository.deactivateAllVersionsByCdId(cdId);
            log.info("ConceptDescription [{}] 기존 버전들 비활성화 완료", cdId);
        }

        int newVersion = createNewVersion || maxVersion == 0 ? maxVersion + 1 : maxVersion;

        ConceptDescriptionEntity entity = ConceptDescriptionEntity.builder()
                .cdId(cdId)
                .idShort(idShort)
                .cdJson(cdJson)
                .version(newVersion)
                .isActive(true)
                .createdBy(username)
                .build();

        ConceptDescriptionEntity saved = conceptDescriptionRepository.save(entity);
        log.info("ConceptDescription 저장 완료: id={}, version={}, createdBy={}", cdId, newVersion, username);

        return saved;
    }

    /**
     * ConceptDescription 버전 목록 조회
     */
    @Transactional(readOnly = true)
    public List<VersionInfo> getConceptDescriptionVersions(String cdId) {
        return conceptDescriptionRepository.findByCdIdOrderByVersionDesc(cdId).stream()
                .map(this::toVersionInfo)
                .collect(Collectors.toList());
    }

    /**
     * ConceptDescription 특정 버전 활성화
     */
    @Transactional
    public ConceptDescriptionEntity activateConceptDescriptionVersion(String cdId, Integer version, String username) {
        conceptDescriptionRepository.deactivateAllVersionsByCdId(cdId);

        ConceptDescriptionEntity entity = conceptDescriptionRepository.findByCdIdAndVersion(cdId, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ConceptDescription not found: cdId=" + cdId + ", version=" + version));

        entity.setIsActive(true);
        entity.setUpdatedBy(username);

        log.info("ConceptDescription 버전 활성화: id={}, version={}, activatedBy={}", cdId, version, username);
        return conceptDescriptionRepository.save(entity);
    }

    // ======================= 유틸리티 메서드 =======================

    private VersionInfo toVersionInfo(AasEntity entity) {
        return VersionInfo.builder()
                .seq(entity.getSeq())
                .entityId(entity.getAasId())
                .idShort(entity.getIdShort())
                .version(entity.getVersion())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private VersionInfo toVersionInfo(SubmodelEntity entity) {
        return VersionInfo.builder()
                .seq(entity.getSeq())
                .entityId(entity.getSubmodelId())
                .idShort(entity.getIdShort())
                .version(entity.getVersion())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private VersionInfo toVersionInfo(ConceptDescriptionEntity entity) {
        return VersionInfo.builder()
                .seq(entity.getSeq())
                .entityId(entity.getCdId())
                .idShort(entity.getIdShort())
                .version(entity.getVersion())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
