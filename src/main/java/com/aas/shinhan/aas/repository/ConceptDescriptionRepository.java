package com.aas.shinhan.aas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aas.shinhan.aas.entity.ConceptDescriptionEntity;

@Repository
public interface ConceptDescriptionRepository extends JpaRepository<ConceptDescriptionEntity, Long> {

    // 특정 CD ID의 모든 버전 조회
    List<ConceptDescriptionEntity> findByCdIdOrderByVersionDesc(String cdId);

    // 특정 CD ID의 활성화된 버전 조회
    Optional<ConceptDescriptionEntity> findByCdIdAndIsActiveTrue(String cdId);

    // 특정 CD ID와 버전으로 조회
    Optional<ConceptDescriptionEntity> findByCdIdAndVersion(String cdId, Integer version);

    // 특정 CD ID의 최신 버전 번호 조회
    @Query("SELECT MAX(c.version) FROM ConceptDescriptionEntity c WHERE c.cdId = :cdId")
    Optional<Integer> findMaxVersionByCdId(@Param("cdId") String cdId);

    // 특정 CD ID의 모든 버전 비활성화
    @Modifying
    @Query("UPDATE ConceptDescriptionEntity c SET c.isActive = false WHERE c.cdId = :cdId")
    void deactivateAllVersionsByCdId(@Param("cdId") String cdId);

    // 활성화된 모든 CD 조회
    List<ConceptDescriptionEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    // CD ID 존재 여부 확인
    boolean existsByCdId(String cdId);
}
