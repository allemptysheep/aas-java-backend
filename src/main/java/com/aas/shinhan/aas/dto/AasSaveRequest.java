package com.aas.shinhan.aas.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * AAS 저장 요청 DTO
 */
@Getter
@Setter
@Builder
public class AasSaveRequest {
    private String aasId;
    private String idShort;
    private String assetKind;
    private String globalAssetId;
    private String aasJson;
    private boolean createNewVersion;  // true면 새 버전 생성, false면 기존 버전 업데이트
}
