package com.aas.shinhan.aas.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AasUploadResponse {
    private List<String> aasIds;
    private List<String> submodelIds;
    private List<String> conceptDescriptionIds;
    private String message;
}
