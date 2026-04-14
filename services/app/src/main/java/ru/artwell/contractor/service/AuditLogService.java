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

/**
 * Сервис для работы с журналом аудита (audit_log).
 *
 * Audit log — это read-only для пользователей API. Записи создаются
 * автоматически при значимых действиях (создание, обновление, удаление
 * документов, пользователей и т.д.) и не могут быть изменены или удалены.
 *
 * Пользователи могут только просматривать записи с фильтрами:
 * по пользователю, типу действия, типу сущности и временному диапазону.
 */
@Service
public class AuditLogService {

    private final ZoneId applicationZoneId;
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                           AuditLogRepository auditLogRepository) {
        this.applicationZoneId = applicationZoneId;
        this.auditLogRepository = auditLogRepository;
    }

    // ─── Чтение (только!) ───────────────────────────────────────

    /**
     * Поиск с множественными фильтрами.
     * Все параметры опциональны — пустые/null пропускаются.
     *
     * @param userId      фильтр по ID пользователя
     * @param action      фильтр по действию (CREATE, UPDATE, DELETE, ...)
     * @param entityType  фильтр по типу сущности (Document, User, ...)
     * @param from        начало временного диапазона
     * @param to          конец временного диапазона
     * @param pageable    пагинация
     * @return страница записей аудита, отсортированных по timestamp DESC
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(Long userId, String action, String entityType,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.search(userId, action, entityType, from, to, pageable)
                .map(this::toResponse);
    }

    /** Все записи аудита для конкретного пользователя */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(this::toResponse);
    }

    /** Все записи аудита для конкретной сущности (например, все действия с документом #5) */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    /** Детали записи аудита */
    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        return toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Audit log entry not found: " + id)));
    }

    // ─── Запись (для внутреннего использования сервисами) ───────

    /**
     * Записать действие в audit log.
     * Вызывается из других сервисов при создании, обновлении, удалении сущностей.
     * Старое и новое значение сериализуются в JSON или текст.
     *
     * @param userId      ID пользователя, выполнившего действие
     * @param username    username пользователя (для быстрого чтения без join)
     * @param action      тип действия (CREATE, UPDATE, DELETE)
     * @param entityType  тип сущности (Document, User, Organization, ...)
     * @param entityId    ID сущности, над которой performed действие
     * @param oldValue    старое значение (JSON или null для CREATE)
     * @param newValue    новое значение (JSON или null для DELETE)
     * @param ipAddress   IP-адрес клиента
     */
    @Transactional
    public void log(Long userId, String username, String action,
                    String entityType, Long entityId, String oldValue,
                    String newValue, String ipAddress) {
        AuditLogEntity entry = new AuditLogEntity(
                userId, username, action, entityType, entityId,
                oldValue, newValue, ipAddress, LocalDateTime.now(applicationZoneId)
        );
        auditLogRepository.save(entry);
    }

    // ─── Вспомогательные ────────────────────────────────────────

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