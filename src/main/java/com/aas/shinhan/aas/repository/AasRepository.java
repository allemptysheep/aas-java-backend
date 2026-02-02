package com.aas.shinhan.aas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aas.shinhan.aas.entity.AasEntity;

@Repository
public interface AasRepository extends JpaRepository<AasEntity, Long> {

    // 특정 AAS ID의 모든 버전 조회
    List<AasEntity> findByAasIdOrderByVersionDesc(String aasId);

    // 특정 AAS ID의 활성화된 버전 조회
    Optional<AasEntity> findByAasIdAndIsActiveTrue(String aasId);

    // 특정 AAS ID와 버전으로 조회
    Optional<AasEntity> findByAasIdAndVersion(String aasId, Integer version);

    // 특정 AAS ID의 최신 버전 번호 조회
    @Query("SELECT MAX(a.version) FROM AasEntity a WHERE a.aasId = :aasId")
    Optional<Integer> findMaxVersionByAasId(@Param("aasId") String aasId);

    // 특정 AAS ID의 모든 버전 비활성화
    @Modifying
    @Query("UPDATE AasEntity a SET a.isActive = false WHERE a.aasId = :aasId")
    void deactivateAllVersionsByAasId(@Param("aasId") String aasId);

    // 활성화된 모든 AAS 조회
    List<AasEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    // AAS ID 존재 여부 확인
    boolean existsByAasId(String aasId);

    // 특정 AAS ID의 버전 개수 조회
    long countByAasId(String aasId);
}
