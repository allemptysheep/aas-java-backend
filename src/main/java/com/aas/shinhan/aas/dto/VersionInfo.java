package com.aas.shinhan.aas.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 버전 정보 DTO
 * BaSyx 프론트엔드의 AasVersionInfo 인터페이스를 참고하여 설계
 */
@Getter
@Builder
public class VersionInfo {
    private Long seq;
    private String entityId;          // AAS ID, Submodel ID, or CD ID
    private String idShort;
    private Integer version;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
