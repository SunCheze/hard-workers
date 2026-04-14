package ru.artwell.contractor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.DocumentVersionDetailResponse;
import ru.artwell.contractor.persistence.entity.DocumentEntity;
import ru.artwell.contractor.persistence.entity.DocumentVersionEntity;
import ru.artwell.contractor.persistence.entity.VersionValidationStatus;
import ru.artwell.contractor.persistence.repository.DocumentRepository;
import ru.artwell.contractor.persistence.repository.DocumentVersionRepository;

import java.util.List;

/**
 * Сервис для работы с версиями документов.
 *
 * Предоставляет:
 * — просмотр списка версий документа (от новых к старым);
 * — просмотр деталей конкретной версии;
 * — откат документа к указанной версии (создаёт новую версию с содержимым старой).
 *
 * Версии образуют связный список через previous_version_id: каждая новая версия
 * ссылается на предыдущую. Это позволяет восстановить полную историю изменений.
 */
@Service
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;

    public DocumentVersionService(DocumentVersionRepository documentVersionRepository,
                                  DocumentRepository documentRepository) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentRepository = documentRepository;
    }

    // ─── Чтение ─────────────────────────────────────────────────

    /**
     * Получить список всех версий документа, отсортированных от новых к старым.
     *
     * @param documentId ID документа (не версии!)
     * @return список DTO с базовой информацией о каждой версии
     */
    @Transactional(readOnly = true)
    public List<DocumentVersionDetailResponse> listByDocument(Long documentId) {
        findDocumentOrThrow(documentId);

        return documentVersionRepository.findByDocument_IdOrderByVersionNumberDesc(documentId).stream()
                .map(this::toDetailResponse)
                .toList();
    }

    /**
     * Получить детали конкретной версии документа.
     *
     * @param versionId ID версии
     * @return DTO с полной информацией о версии
     */
    @Transactional(readOnly = true)
    public DocumentVersionDetailResponse getById(Long versionId) {
        DocumentVersionEntity version = documentVersionRepository.findByIdWithDocument(versionId)
                .orElseThrow(() -> new NotFoundException("Document version not found: " + versionId));
        return toDetailResponse(version);
    }

    // ─── Откат ──────────────────────────────────────────────────

    /**
     * Откатить документ к указанной версии.
     *
     * Как работает:
     * 1. Находит целевую версию по ID
     * 2. Находит текущую (последнюю) версию документа
     * 3. Создаёт НОВУЮ версию с тем же содержимым XML, что и целевая
     * 4. Увеличивает номер версии: currentVersion + 1
     * 5. Новая версия ссылается на предыдущую (текущую)
     * 6. Обновляет currentVersion в документе
     *
     * Почему создаётся новая версия, а не удаляются промежуточные?
     * Потому что история изменений должна сохраняться для audit-целей.
     * Откат — это не удаление, а создание «восстановительной» версии.
     *
     * @param versionId ID целевой версии, к которой нужно откатиться
     * @return DTO созданной (новой) версии
     */
    @Transactional
    public DocumentVersionDetailResponse revertTo(Long versionId) {
        DocumentVersionEntity targetVersion = documentVersionRepository.findByIdWithDocument(versionId)
                .orElseThrow(() -> new NotFoundException("Document version not found: " + versionId));

        DocumentEntity document = targetVersion.getDocument();

        DocumentVersionEntity currentLatest = documentVersionRepository
                .findTopByDocument_IdOrderByVersionNumberDesc(document.getId())
                .orElseThrow(() -> new NotFoundException("No versions found for document: " + document.getId()));

        int nextVersionNumber = currentLatest.getVersionNumber() + 1;

        // Создаём новую версию, копируя содержимое целевой
        DocumentVersionEntity revertedVersion = new DocumentVersionEntity(
                document,
                nextVersionNumber,
                targetVersion.getXmlFilePath(),   // указываем на тот же XML-файл
                targetVersion.getXmlFileName(),
                targetVersion.getXmlFileSize(),
                VersionValidationStatus.VALID,     // откатываемая версия считается валидной
                null,                              // ошибок валидации нет
                currentLatest.getUploadedBy(),     // автор откатки = автор последней версии
                java.time.LocalDateTime.now(java.time.ZoneId.of("Europe/Moscow")),
                currentLatest                      // предыдущая = текущая последняя
        );

        DocumentVersionEntity saved = documentVersionRepository.save(revertedVersion);

        document.setCurrentVersion(nextVersionNumber);
        documentRepository.save(document);

        return toDetailResponse(saved);
    }

    // ─── Вспомогательные ────────────────────────────────────────

    private DocumentEntity findDocumentOrThrow(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));
    }

    private DocumentVersionDetailResponse toDetailResponse(DocumentVersionEntity v) {
        return new DocumentVersionDetailResponse(
                v.getId(),
                v.getDocument().getId(),
                v.getDocument().getDocumentNumber(),
                v.getDocument().getDocumentType().getTypeCode(),
                v.getVersionNumber(),
                v.getXmlFileName(),
                v.getXmlFileSize(),
                v.getValidationStatus().name(),
                v.getValidationErrors(),
                v.getUploadedBy() != null ? v.getUploadedBy().getUsername() : null,
                v.getUploadedAt(),
                v.getPreviousVersion() != null ? v.getPreviousVersion().getId() : null
        );
    }

    // ─── Исключения ─────────────────────────────────────────────

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
