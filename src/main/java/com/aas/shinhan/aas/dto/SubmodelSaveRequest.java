package com.aas.shinhan.aas.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Submodel 저장 요청 DTO
 */
@Getter
@Setter
@Builder
public class SubmodelSaveRequest {
    private String submodelId;
    private String idShort;
    private String semanticId;
    private String aasId;
    private String submodelJson;
    private boolean createNewVersion;  // true면 새 버전 생성, false면 기존 버전 업데이트
}
