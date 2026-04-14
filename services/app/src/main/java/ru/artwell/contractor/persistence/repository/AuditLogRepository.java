package ru.artwell.contractor.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.artwell.contractor.persistence.entity.AuditLogEntity;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    Page<AuditLogEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogEntity> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE (:userId IS NULL OR a.user.id = :userId) " +
            "AND (:action IS NULL OR a.actionType = :action) " +
            "AND (:entityType IS NULL OR a.entityType = :entityType) " +
            "AND (:from IS NULL OR a.createdAt >= :from) " +
            "AND (:to IS NULL OR a.createdAt <= :to) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> search(@Param("userId") Long userId,
                                @Param("action") String action,
                                @Param("entityType") String entityType,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);
}