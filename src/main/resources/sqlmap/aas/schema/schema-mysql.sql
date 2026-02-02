-- =============================================
-- Shinhan AAS Server - MySQL DDL Script
-- =============================================

-- =============================================
-- AAS (Asset Administration Shell) 테이블
-- =============================================
CREATE TABLE AAS_SHELL (
    SEQ                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    AAS_ID              VARCHAR(500) NOT NULL,
    ID_SHORT            VARCHAR(200),
    ASSET_KIND          VARCHAR(50),
    GLOBAL_ASSET_ID     VARCHAR(500),
    VERSION             INT NOT NULL,
    IS_ACTIVE           TINYINT(1) DEFAULT 1 NOT NULL,
    AAS_JSON            LONGTEXT,
    CREATED_BY          VARCHAR(100),
    CREATED_AT          DATETIME NOT NULL,
    UPDATED_BY          VARCHAR(100),
    UPDATED_AT          DATETIME,

    INDEX IDX_AAS_AAS_ID (AAS_ID),
    INDEX IDX_AAS_IS_ACTIVE (IS_ACTIVE),
    INDEX IDX_AAS_AAS_ID_VERSION (AAS_ID, VERSION),
    INDEX IDX_AAS_CREATED_AT (CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Asset Administration Shell 저장 테이블';

-- =============================================
-- Submodel 테이블
-- =============================================
CREATE TABLE AAS_SUBMODEL (
    SEQ                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    SUBMODEL_ID         VARCHAR(500) NOT NULL,
    ID_SHORT            VARCHAR(200),
    SEMANTIC_ID         VARCHAR(500),
    AAS_ID              VARCHAR(500),
    VERSION             INT NOT NULL,
    IS_ACTIVE           TINYINT(1) DEFAULT 1 NOT NULL,
    SUBMODEL_JSON       LONGTEXT,
    CREATED_BY          VARCHAR(100),
    CREATED_AT          DATETIME NOT NULL,
    UPDATED_BY          VARCHAR(100),
    UPDATED_AT          DATETIME,

    INDEX IDX_SM_SUBMODEL_ID (SUBMODEL_ID),
    INDEX IDX_SM_AAS_ID (AAS_ID),
    INDEX IDX_SM_IS_ACTIVE (IS_ACTIVE),
    INDEX IDX_SM_SUBMODEL_ID_VERSION (SUBMODEL_ID, VERSION),
    INDEX IDX_SM_CREATED_AT (CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Submodel 저장 테이블';

-- =============================================
-- ConceptDescription 테이블
-- =============================================
CREATE TABLE AAS_CONCEPT_DESCRIPTION (
    SEQ                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    CD_ID               VARCHAR(500) NOT NULL,
    ID_SHORT            VARCHAR(200),
    VERSION             INT NOT NULL,
    IS_ACTIVE           TINYINT(1) DEFAULT 1 NOT NULL,
    CD_JSON             LONGTEXT,
    CREATED_BY          VARCHAR(100),
    CREATED_AT          DATETIME NOT NULL,
    UPDATED_BY          VARCHAR(100),
    UPDATED_AT          DATETIME,

    INDEX IDX_CD_CD_ID (CD_ID),
    INDEX IDX_CD_IS_ACTIVE (IS_ACTIVE),
    INDEX IDX_CD_CD_ID_VERSION (CD_ID, VERSION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ConceptDescription 저장 테이블';
