package services.app.src.main.java.ru.artwell.contractor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.NotificationCreateRequest;
import ru.artwell.contractor.dto.NotificationResponse;
import ru.artwell.contractor.persistence.entity.NotificationEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.NotificationRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Сервис для работы с уведомлениями пользователей (notifications).
 *
 * Предоставляет:
 * — создание уведомлений (обычно из других сервисов);
 * — просмотр уведомлений пользователя (все / только непрочитанные);
 * — отметка уведомления как прочитанное;
 * — отметка ВСЕХ уведомлений пользователя как прочитанные;
 * — подсчёт непрочитанных уведомлений.
 *
 * Типы уведомлений: DOCUMENT_UPLOADED, APPROVAL_REQUIRED, APPROVED,
 * REJECTED, STATUS_CHANGED, MENTION, SYSTEM.
 */
@Service
public class NotificationService {

    private final ZoneId applicationZoneId;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.applicationZoneId = applicationZoneId;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // ─── Чтение ─────────────────────────────────────────────────

    /** Все уведомления пользователя (от новых к старым) */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listByUser(Long userId, Pageable pageable) {
        findUserOrThrow(userId);
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    /** Только непрочитанные уведомления */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listUnreadByUser(Long userId, Pageable pageable) {
        findUserOrThrow(userId);
        return notificationRepository.findByUser_IdAndReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    /** Количество непрочитанных уведомлений */
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    // ─── Создание ───────────────────────────────────────────────

    /** Создать уведомление для пользователя */
    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + request.getUserId()));

        String type = (request.getType() != null && !request.getType().isBlank())
                ? request.getType() : "SYSTEM";

        NotificationEntity entity = new NotificationEntity(
                user, request.getTitle(), request.getMessage(),
                type, false, LocalDateTime.now(applicationZoneId), null
        );
        return toResponse(notificationRepository.save(entity));
    }

    // ─── Отметить как прочитанное ───────────────────────────────

    /** Отметить одно уведомление как прочитанное */
    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        NotificationEntity entity = notificationRepository.findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));

        if (!entity.isRead()) {
            entity.setRead(true);
            entity.setReadAt(LocalDateTime.now(applicationZoneId));
            notificationRepository.save(entity);
        }
        return toResponse(entity);
    }

    /** Отметить ВСЕ непрочитанные уведомления пользователя как прочитанные */
    @Transactional
    public long markAllAsRead(Long userId) {
        findUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now(applicationZoneId);
        var unread = notificationRepository.findByUser_IdAndReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged());
        long count = 0;
        for (NotificationEntity entity : unread) {
            entity.setRead(true);
            entity.setReadAt(now);
            notificationRepository.save(entity);
            count++;
        }
        return count;
    }

    // ─── Вспомогательные ────────────────────────────────────────

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    private NotificationResponse toResponse(NotificationEntity e) {
        return new NotificationResponse(
                e.getId(), e.getUser().getId(), e.getTitle(), e.getMessage(),
                e.getType(), e.isRead(), e.getCreatedAt(), e.getReadAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}

