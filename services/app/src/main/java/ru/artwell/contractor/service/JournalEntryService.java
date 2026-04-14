package ru.artwell.contractor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.JournalEntryRequest;
import ru.artwell.contractor.dto.JournalEntryResponse;
import ru.artwell.contractor.persistence.entity.DocumentEntity;
import ru.artwell.contractor.persistence.entity.JournalEntryEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.DocumentRepository;
import ru.artwell.contractor.persistence.repository.JournalEntryRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Сервис для работы с журнальными записями документов.
 *
 * Журнал — это append-only лог действий с документом: stamp, утверждение,
 * отклонение, смена статуса, комментарии. Записи невозможно редактировать
 * или удалять (только добавлять) для обеспечения audit-целостности.
 *
 * Журнал используется для отслеживания жизненного цикла документа
 * и формирования истории работы с ним.
 */
@Service
public class JournalEntryService {

    private static final String DEFAULT_TYPE = "STATUS_CHANGE";

    private final ZoneId applicationZoneId;
    private final JournalEntryRepository journalEntryRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public JournalEntryService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                               JournalEntryRepository journalEntryRepository,
                               DocumentRepository documentRepository,
                               UserRepository userRepository) {
        this.applicationZoneId = applicationZoneId;
        this.journalEntryRepository = journalEntryRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    // ─── Чтение ─────────────────────────────────────────────────

    /** Список записей журнала документа (пагинация, от новых к старым) */
    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> listByDocument(Long documentId, Pageable pageable) {
        findDocumentOrThrow(documentId);
        return journalEntryRepository.findByDocument_IdOrderByPerformedAtDesc(documentId, pageable)
                .map(this::toResponse);
    }

    /** Список записей, выполненных пользователем */
    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> listByUser(Long userId, Pageable pageable) {
        findUserOrThrow(userId);
        return journalEntryRepository.findByPerformedBy_IdOrderByPerformedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    /** Детали записи журнала */
    @Transactional(readOnly = true)
    public JournalEntryResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Создание (append-only) ─────────────────────────────────

    /**
     * Добавить запись в журнал документа.
     * Записи журнала immutable — нельзя редактировать или удалить.
     */
    @Transactional
    public JournalEntryResponse create(JournalEntryRequest request) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new NotFoundException("Document not found: " + request.getDocumentId()));

        UserEntity performedBy = userRepository.findById(request.getPerformedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getPerformedByUserId()));

        String type = (request.getJournalType() != null && !request.getJournalType().isBlank())
                ? request.getJournalType() : DEFAULT_TYPE;

        JournalEntryEntity entity = new JournalEntryEntity(
                document,
                request.getAction(),
                request.getComment(),
                performedBy,
                LocalDateTime.now(applicationZoneId),
                type
        );

        return toResponse(journalEntryRepository.save(entity));
    }

    // ─── Вспомогательные ────────────────────────────────────────

    JournalEntryEntity findOrThrow(Long id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Journal entry not found: " + id));
    }

    private DocumentEntity findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    }

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    private JournalEntryResponse toResponse(JournalEntryEntity e) {
        return new JournalEntryResponse(
                e.getId(),
                e.getDocument().getId(),
                e.getAction(),
                e.getComment(),
                e.getPerformedBy().getId(),
                e.getPerformedBy().getUsername(),
                e.getPerformedAt(),
                e.getJournalType()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}

