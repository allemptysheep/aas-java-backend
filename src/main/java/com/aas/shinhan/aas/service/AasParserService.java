package com.aas.shinhan.aas.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aas.shinhan.aas.dto.AasSaveRequest;
import com.aas.shinhan.aas.dto.AasUploadResponse;
import com.aas.shinhan.aas.dto.SubmodelSaveRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AasParserService {

    private final AasStorageService aasStorageService;
    private final JsonSerializer jsonSerializer = new JsonSerializer();

    public AasUploadResponse parseAndProcess(MultipartFile file, String uploadedBy, boolean ignoreDuplicates) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is required");
        }

        Environment environment = parseFile(file);
        return processEnvironment(environment, uploadedBy, ignoreDuplicates);
    }

    private Environment parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename().toLowerCase();
        InputStream inputStream = file.getInputStream();

        if (filename.endsWith(".aasx")) {
            return parseAasx(inputStream);
        } else if (filename.endsWith(".json")) {
            return parseJson(inputStream);
        } else if (filename.endsWith(".xml")) {
            return parseXml(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Supported: .aasx, .json, .xml");
        }
    }

    private Environment parseAasx(InputStream inputStream) throws Exception {
        AASXDeserializer deserializer = new AASXDeserializer(inputStream);
        return deserializer.read();
    }

    private Environment parseJson(InputStream inputStream) throws Exception {
        JsonDeserializer deserializer = new JsonDeserializer();
        return deserializer.read(inputStream, Environment.class);
    }

    private Environment parseXml(InputStream inputStream) throws Exception {
        XmlDeserializer deserializer = new XmlDeserializer();
        return deserializer.read(inputStream);
    }

    @Transactional
    private AasUploadResponse processEnvironment(Environment environment, String uploadedBy, boolean ignoreDuplicates) {
        List<String> aasIds = new ArrayList<>();
        List<String> submodelIds = new ArrayList<>();
        List<String> conceptDescriptionIds = new ArrayList<>();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("========================================");
        log.info("AAS Environment Parsing Result");
        log.info("========================================");
        log.info("Uploaded by: {}", uploadedBy);
        log.info("Timestamp: {}", timestamp);
        log.info("Ignore duplicates: {}", ignoreDuplicates);
        log.info("----------------------------------------");

        // AAS 처리 및 저장
        if (environment.getAssetAdministrationShells() != null) {
            log.info("[AssetAdministrationShells] Count: {}", environment.getAssetAdministrationShells().size());
            for (AssetAdministrationShell aas : environment.getAssetAdministrationShells()) {
                String aasId = aas.getId();
                aasIds.add(aasId);

                log.info("  AAS:");
                log.info("    - id: {}", aasId);
                log.info("    - idShort: {}", aas.getIdShort());

                String assetKind = null;
                String globalAssetId = null;
                if (aas.getAssetInformation() != null) {
                    assetKind = aas.getAssetInformation().getAssetKind() != null
                            ? aas.getAssetInformation().getAssetKind().name() : null;
                    globalAssetId = aas.getAssetInformation().getGlobalAssetId();
                    log.info("    - assetKind: {}", assetKind);
                    log.info("    - globalAssetId: {}", globalAssetId);
                }
                if (aas.getSubmodels() != null) {
                    log.info("    - submodel references: {}", aas.getSubmodels().size());
                    for (Reference ref : aas.getSubmodels()) {
                        if (ref.getKeys() != null && !ref.getKeys().isEmpty()) {
                            log.info("      - {}", ref.getKeys().get(0).getValue());
                        }
                    }
                }

                // AAS를 데이터베이스에 저장
                try {
                    String aasJson = jsonSerializer.write(aas);
                    AasSaveRequest saveRequest = AasSaveRequest.builder()
                            .aasId(aasId)
                            .idShort(aas.getIdShort())
                            .assetKind(assetKind)
                            .globalAssetId(globalAssetId)
                            .aasJson(aasJson)
                            .createNewVersion(!ignoreDuplicates)
                            .build();
                    aasStorageService.saveAas(saveRequest, uploadedBy);
                    log.info("    -> DB 저장 완료");
                } catch (Exception e) {
                    log.error("    -> DB 저장 실패: {}", e.getMessage());
                }
            }
        }

        log.info("----------------------------------------");

        // Submodel 처리 및 저장
        if (environment.getSubmodels() != null) {
            log.info("[Submodels] Count: {}", environment.getSubmodels().size());
            for (Submodel submodel : environment.getSubmodels()) {
                String smId = submodel.getId();
                submodelIds.add(smId);

                log.info("  Submodel:");
                log.info("    - id: {}", smId);
                log.info("    - idShort: {}", submodel.getIdShort());

                String semanticIdStr = null;
                if (submodel.getSemanticId() != null && submodel.getSemanticId().getKeys() != null
                        && !submodel.getSemanticId().getKeys().isEmpty()) {
                    semanticIdStr = submodel.getSemanticId().getKeys().get(0).getValue();
                }
                log.info("    - semanticId: {}", semanticIdStr);

                if (submodel.getSubmodelElements() != null) {
                    log.info("    - submodelElements: {} items", submodel.getSubmodelElements().size());
                    for (SubmodelElement element : submodel.getSubmodelElements()) {
                        logSubmodelElement(element, "      ");
                    }
                }

                // 연결된 AAS ID 찾기 (첫 번째 AAS와 연결)
                String linkedAasId = aasIds.isEmpty() ? null : aasIds.get(0);

                // Submodel을 데이터베이스에 저장
                try {
                    String submodelJson = jsonSerializer.write(submodel);
                    SubmodelSaveRequest saveRequest = SubmodelSaveRequest.builder()
                            .submodelId(smId)
                            .idShort(submodel.getIdShort())
                            .semanticId(semanticIdStr)
                            .aasId(linkedAasId)
                            .submodelJson(submodelJson)
                            .createNewVersion(!ignoreDuplicates)
                            .build();
                    aasStorageService.saveSubmodel(saveRequest, uploadedBy);
                    log.info("    -> DB 저장 완료");
                } catch (Exception e) {
                    log.error("    -> DB 저장 실패: {}", e.getMessage());
                }
            }
        }

        log.info("----------------------------------------");

        // ConceptDescription 처리 및 저장
        if (environment.getConceptDescriptions() != null) {
            log.info("[ConceptDescriptions] Count: {}", environment.getConceptDescriptions().size());
            for (ConceptDescription cd : environment.getConceptDescriptions()) {
                String cdId = cd.getId();
                conceptDescriptionIds.add(cdId);

                log.info("  ConceptDescription:");
                log.info("    - id: {}", cdId);
                log.info("    - idShort: {}", cd.getIdShort());

                // ConceptDescription을 데이터베이스에 저장
                try {
                    String cdJson = jsonSerializer.write(cd);
                    aasStorageService.saveConceptDescription(cdId, cd.getIdShort(), cdJson, !ignoreDuplicates, uploadedBy);
                    log.info("    -> DB 저장 완료");
                } catch (Exception e) {
                    log.error("    -> DB 저장 실패: {}", e.getMessage());
                }
            }
        }

        log.info("========================================");
        log.info("Summary: {} AAS, {} Submodels, {} ConceptDescriptions",
                aasIds.size(), submodelIds.size(), conceptDescriptionIds.size());
        log.info("========================================");

        return AasUploadResponse.builder()
                .aasIds(aasIds)
                .submodelIds(submodelIds)
                .conceptDescriptionIds(conceptDescriptionIds)
                .message("Successfully parsed and saved " + aasIds.size() + " AAS, "
                        + submodelIds.size() + " Submodels, "
                        + conceptDescriptionIds.size() + " ConceptDescriptions"
                        + " (uploaded by: " + uploadedBy + ")")
                .build();
    }

    private void logSubmodelElement(SubmodelElement element, String indent) {
        String type = element.getClass().getSimpleName().replace("Default", "");

        if (element instanceof Property property) {
            log.info("{}- {} (Property) = {} [{}]",
                    indent, property.getIdShort(), property.getValue(), property.getValueType());

        } else if (element instanceof Range range) {
            log.info("{}- {} (Range) = {} ~ {} [{}]",
                    indent, range.getIdShort(), range.getMin(), range.getMax(), range.getValueType());

        } else if (element instanceof File file) {
            log.info("{}- {} (File) = {} [{}]",
                    indent, file.getIdShort(), file.getValue(), file.getContentType());

        } else if (element instanceof Blob blob) {
            String blobSize = blob.getValue() != null ? blob.getValue().length + " bytes" : "null";
            log.info("{}- {} (Blob) = {} [{}]",
                    indent, blob.getIdShort(), blobSize, blob.getContentType());

        } else if (element instanceof MultiLanguageProperty mlp) {
            log.info("{}- {} (MultiLanguageProperty) = {}",
                    indent, mlp.getIdShort(), mlp.getValue());

        } else if (element instanceof ReferenceElement refElem) {
            String refValue = refElem.getValue() != null && refElem.getValue().getKeys() != null
                    && !refElem.getValue().getKeys().isEmpty()
                    ? refElem.getValue().getKeys().get(0).getValue() : "null";
            log.info("{}- {} (ReferenceElement) -> {}",
                    indent, refElem.getIdShort(), refValue);

        } else if (element instanceof SubmodelElementCollection collection) {
            log.info("{}- {} (Collection) [{} items]",
                    indent, collection.getIdShort(),
                    collection.getValue() != null ? collection.getValue().size() : 0);
            if (collection.getValue() != null) {
                for (SubmodelElement child : collection.getValue()) {
                    logSubmodelElement(child, indent + "  ");
                }
            }

        } else if (element instanceof SubmodelElementList list) {
            log.info("{}- {} (List) [{} items]",
                    indent, list.getIdShort(),
                    list.getValue() != null ? list.getValue().size() : 0);
            if (list.getValue() != null) {
                int idx = 0;
                for (SubmodelElement child : list.getValue()) {
                    log.info("{}  [{}]:", indent, idx++);
                    logSubmodelElement(child, indent + "    ");
                }
            }

        } else {
            log.info("{}- {} ({})",
                    indent, element.getIdShort(), type);
        }
    }
}
