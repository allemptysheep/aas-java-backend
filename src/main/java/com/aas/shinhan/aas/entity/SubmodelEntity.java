package com.aas.shinhan.aas.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AAS_SUBMODEL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmodelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submodel_seq")
    @SequenceGenerator(name = "submodel_seq", sequenceName = "SUBMODEL_SEQ", allocationSize = 1)
    private Long seq;

    @Column(name = "SUBMODEL_ID", nullable = false, length = 500)
    private String submodelId;

    @Column(name = "ID_SHORT", length = 200)
    private String idShort;

    @Column(name = "SEMANTIC_ID", length = 500)
    private String semanticId;

    @Column(name = "AAS_ID", length = 500)
    private String aasId;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Lob
    @Column(name = "SUBMODEL_JSON", columnDefinition = "CLOB")
    private String submodelJson;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.version == null) {
            this.version = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
