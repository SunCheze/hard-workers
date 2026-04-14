package ru.artwell.contractor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.AuditLogResponse;
import ru.artwell.contractor.persistence.entity.AuditLogEntity;
import ru.artwell.contractor.persistence.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.artwell.contractor.config.AppTimeConfiguration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class AuditLogService {

    private final ZoneId applicationZoneId;
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                           AuditLogRepository auditLogRepository) {
        this.applicationZoneId = applicationZoneId;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(Long userId, String action, String entityType,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.search(userId, action, entityType, from, to, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        return toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Audit log entry not found: " + id)));
    }

    @Transactional
    public void log(Long userId, String username, String action,
                    String entityType, Long entityId, String oldValue,
                    String newValue, String ipAddress) {
        AuditLogEntity entry = new AuditLogEntity(
                null, username, action, entityType, entityId,
                null, ipAddress, LocalDateTime.now(applicationZoneId)
        );
        auditLogRepository.save(entry);
    }

    private AuditLogResponse toResponse(AuditLogEntity e) {
        return new AuditLogResponse(
                e.getId(), e.getUserId(), e.getUsername(), e.getAction(),
                e.getEntityType(), e.getEntityId(), e.getOldValue(),
                e.getNewValue(), e.getIpAddress(), e.getTimestamp()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}