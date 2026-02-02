package com.aas.shinhan.aas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aas.shinhan.aas.entity.SubmodelEntity;

@Repository
public interface SubmodelRepository extends JpaRepository<SubmodelEntity, Long> {

    // 특정 Submodel ID의 모든 버전 조회
    List<SubmodelEntity> findBySubmodelIdOrderByVersionDesc(String submodelId);

    // 특정 Submodel ID의 활성화된 버전 조회
    Optional<SubmodelEntity> findBySubmodelIdAndIsActiveTrue(String submodelId);

    // 특정 Submodel ID와 버전으로 조회
    Optional<SubmodelEntity> findBySubmodelIdAndVersion(String submodelId, Integer version);

    // 특정 AAS에 연결된 활성화된 Submodel 목록 조회
    List<SubmodelEntity> findByAasIdAndIsActiveTrueOrderByIdShort(String aasId);

    // 특정 Submodel ID의 최신 버전 번호 조회
    @Query("SELECT MAX(s.version) FROM SubmodelEntity s WHERE s.submodelId = :submodelId")
    Optional<Integer> findMaxVersionBySubmodelId(@Param("submodelId") String submodelId);

    // 특정 Submodel ID의 모든 버전 비활성화
    @Modifying
    @Query("UPDATE SubmodelEntity s SET s.isActive = false WHERE s.submodelId = :submodelId")
    void deactivateAllVersionsBySubmodelId(@Param("submodelId") String submodelId);

    // 활성화된 모든 Submodel 조회
    List<SubmodelEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    // Submodel ID 존재 여부 확인
    boolean existsBySubmodelId(String submodelId);

    // 특정 Submodel ID의 버전 개수 조회
    long countBySubmodelId(String submodelId);
}
