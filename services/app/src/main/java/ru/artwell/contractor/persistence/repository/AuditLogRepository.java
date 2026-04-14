package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.artwell.contractor.persistence.entity.AuditLogEntity;

import java.time.LocalDateTime;
import java.util.Optional;


public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<AuditLogEntity> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogEntity> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE (:userId IS NULL OR a.userId = :userId) " +
            "AND (:action IS NULL OR a.action = :action) " +
            "AND (:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:from IS NULL OR a.timestamp >= :from) " +
            "AND (:to IS NULL OR a.timestamp <= :to) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLogEntity> search(@Param("userId") Long userId,
                                @Param("action") String action,
                                @Param("entityType") String entityType,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);
}
