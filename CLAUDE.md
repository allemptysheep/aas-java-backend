<!-- @format -->

# Shinhan AAS Server - 프로젝트 지침

## 프로젝트 개요

신한 Asset Administration Shell (AAS) 서버 프로젝트입니다.
Eclipse BaSyx Web UI와 연동하여 AAS 데이터를 관리합니다.

## 기술 스택

- **Backend**: Spring Boot 3.5.10, Java 21
- **Security**: JWT 인증 (jjwt 0.12.6)
- **AAS Library**: Eclipse AAS4J 2.0.1
- **Database**: Oracle (운영), MySQL (개발) - 계층화 저장 + 버전 컨트롤
- **Frontend DB**: MongoDB (프론트에서 직접 저장)

## 프로젝트 구조

```
src/main/java/com/aas/shinhan/
├── account/                    # 계정 관련
│   ├── controller/
│   │   └── LoginController.java
│   └── dto/
│       ├── LoginRequest.java
│       └── LoginResponse.java
├── aas/                        # AAS 핵심 기능
│   ├── controller/
│   │   └── AasUploadController.java
│   ├── service/
│   │   └── AasParserService.java
│   └── dto/
│       └── AasUploadResponse.java
├── config/                     # 설정
│   ├── SecurityConfig.java
│   └── PasswordEncoderConfig.java
└── core/                       # 공통 핵심
    ├── jwt/
    │   ├── JwtTokenProvider.java
    │   ├── JwtTokenFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   └── JwtAccessDeniedHandler.java
    └── security/service/
        └── AasUserDetailsService.java
```

## 데이터 흐름

```
[BaSyx Web UI (프론트)]
         │
         ├──(AASX/JSON/XML)──▶ [MongoDB] (직접 저장)
         │
         └──(로그인/업로드)──▶ [Java Backend]
                                    │
                                    ├── JWT 인증
                                    ├── AAS4J 파싱
                                    ├── 계층화 (AAS/Submodel/CD 분리)
                                    └── Oracle/MySQL 저장 + 버전 컨트롤
```

## API 엔드포인트

### 인증

| Method | Endpoint              | 설명          | 인증   |
| ------ | --------------------- | ------------- | ------ |
| POST   | `/accounts/aas/login` | JWT 토큰 발급 | 불필요 |
| GET    | `/accounts/test`      | 연결 테스트   | 불필요 |

### AAS

| Method | Endpoint      | 설명                              | 인증 |
| ------ | ------------- | --------------------------------- | ---- |
| POST   | `/aas/upload` | AASX/JSON/XML 파일 업로드 및 파싱 | 필요 |

## 인증 방식

- JWT Bearer Token
- Access Token: 1시간
- Refresh Token: 7일
- 헤더: `Authorization: Bearer {token}`

## 테스트 계정

- Username: `admin`
- Password: `admin123`

## AAS 데이터 구조

```
AAS (헤더/메타정보)
├── id, idShort
├── assetInformation
└── submodels: [참조만]
         │
         ▼
Submodel (상세 데이터)
├── id, idShort, semanticId
└── submodelElements: [Property, Collection, File, ...]
         │
         ▼
ConceptDescription (의미 정의)
├── id, idShort
└── embeddedDataSpecifications
```

## 데이터베이스

### 지원 DB

| DB     | 용도                   | 프로필                    |
| ------ | ---------------------- | ------------------------- |
| H2     | 개발/테스트 (인메모리) | 기본값 (프로필 미설정 시) |
| Oracle | 운영                   | `oracle`                  |
| MySQL  | 개발/스테이징          | `mysql`                   |

### 테이블 구조

```
┌─────────────────────────────────────────────────────────────┐
│ AAS_SHELL (Asset Administration Shell)                      │
├─────────────────────────────────────────────────────────────┤
│ SEQ (PK), AAS_ID, ID_SHORT, ASSET_KIND, GLOBAL_ASSET_ID,   │
│ VERSION, IS_ACTIVE, AAS_JSON (CLOB),                        │
│ CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│ AAS_SUBMODEL (Submodel)                                     │
├─────────────────────────────────────────────────────────────┤
│ SEQ (PK), SUBMODEL_ID, ID_SHORT, SEMANTIC_ID, AAS_ID,      │
│ VERSION, IS_ACTIVE, SUBMODEL_JSON (CLOB),                   │
│ CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ AAS_CONCEPT_DESCRIPTION (ConceptDescription - 선택적)       │
├─────────────────────────────────────────────────────────────┤
│ SEQ (PK), CD_ID, ID_SHORT, VERSION, IS_ACTIVE,             │
│ CD_JSON (CLOB), CREATED_BY, CREATED_AT, UPDATED_BY, UPDATED_AT │
│                                                             │
│ ※ AASX 파일에 ConceptDescription이 포함된 경우에만 저장됨   │
│ ※ 대부분 외부 레지스트리(ECLASS, IEC CDD) 참조로 사용       │
└─────────────────────────────────────────────────────────────┘
```

### 시퀀스

- `AAS_SEQ` - AAS_SHELL용
- `SUBMODEL_SEQ` - AAS_SUBMODEL용
- `CONCEPT_DESC_SEQ` - AAS_CONCEPT_DESCRIPTION용

### 버전 관리

- 동일 ID로 재업로드 시 새 버전 생성 (VERSION 증가)
- IS_ACTIVE로 활성 버전 관리 (1=활성, 0=비활성)
- 이전 버전은 비활성화되지만 삭제되지 않음 (이력 보존)

### 환경변수 설정

**Oracle 환경 (.env)**

```bash
SPRING_PROFILES_ACTIVE=oracle
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521:ORCL
SPRING_DATASOURCE_USERNAME=AAS
SPRING_DATASOURCE_PASSWORD=aas1234
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

**MySQL 환경 (.env.mysql)**

```bash
SPRING_PROFILES_ACTIVE=mysql
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/aas_db?useSSL=false&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=aas_user
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
```

**개발 환경 (.env.dev)**

```bash
# SPRING_PROFILES_ACTIVE 미설정 → H2 인메모리 사용
SERVER_PORT=6443
SPRING_JPA_SHOW_SQL=true
```

### DDL 스크립트 위치

- Oracle: `src/main/resources/sqlmap/aas/schema/schema-oracle.sql`
- MySQL: `src/main/resources/sqlmap/aas/schema/schema-mysql.sql`

### Oracle 한국어 문자셋 지원

Oracle DB가 `KO16MSWIN949` 문자셋 사용 시 `orai18n` 의존성 필요:

```xml
<dependency>
    <groupId>com.oracle.database.nls</groupId>
    <artifactId>orai18n</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 설정 파일

### application.yaml 주요 설정

- 서버 포트: 6443
- 파일 업로드: 최대 100MB
- JWT Secret: `jwt.secret`

## 개발 지침

### 패키지 구조 규칙

- Controller: `*.controller`
- Service: `*.service`
- DTO: `*.dto`
- Entity: `*.entity`
- Repository: `*.repository`

### 로깅

- 업로드 시 사용자 정보 기록
- AAS 파싱 결과 상세 로그 출력

### 보안

- CORS 전체 허용 (개발용)
- CSRF 비활성화
- Stateless 세션

## TODO

- [x] Oracle/MySQL 연동
- [x] AAS 버전 컨트롤 구현
- [x] AAS/Submodel/CD Repository 구현
- [ ] 조회 API 구현 (GET /aas/shells, /aas/submodels)
- [ ] 실제 사용자 DB 연동 (현재 하드코딩)

## 빌드 및 실행

```bash
# 빌드
mvn clean package -DskipTests

# 로컬 실행
java -jar target/shinhan-0.0.2-SNAPSHOT.jar

# Docker 빌드
docker build -t shinhan-aas-server:0.0.2 .

# Docker 실행 (H2 인메모리 - 개발용)
docker run --rm --name shinhan-aas-server --network aas-net -p 6443:6443 shinhan-aas-server:0.0.2 --spring.profiles.active=local

# Docker 실행 (Oracle - 운영)
docker run --rm --name shinhan-aas-server --network aas-net -p 6443:6443 shinhan-aas-server:0.0.2 --spring.profiles.active=dev

# Docker 실행 (MySQL)
docker run --env-file .env.mysql -p 6443:6443 shinhan-aas-server:0.0.2
```
