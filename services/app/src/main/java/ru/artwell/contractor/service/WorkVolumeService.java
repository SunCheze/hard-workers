package ru.artwell.contractor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.WorkVolumeRequest;
import ru.artwell.contractor.dto.WorkVolumeResponse;
import ru.artwell.contractor.persistence.entity.DocumentEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.entity.WorkVolumeEntity;
import ru.artwell.contractor.persistence.repository.DocumentRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;
import ru.artwell.contractor.persistence.repository.WorkVolumeRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Сервис для работы с объёмами работ (work_volumes).
 *
 * Хранит привязку видов работ к документам: тип работы, объём,
 * единицу измерения, статус утверждения. Каждый объём привязан
 * к конкретному документу и (опционально) к объекту строительства.
 */
@Service
public class WorkVolumeService {

    private final ZoneId applicationZoneId;
    private final WorkVolumeRepository workVolumeRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public WorkVolumeService(@Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId,
                             WorkVolumeRepository workVolumeRepository,
                             DocumentRepository documentRepository,
                             UserRepository userRepository) {
        this.applicationZoneId = applicationZoneId;
        this.workVolumeRepository = workVolumeRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    // ─── Чтение ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<WorkVolumeResponse> listByDocument(Long documentId, Pageable pageable) {
        findDocumentOrThrow(documentId);
        return workVolumeRepository.findByDocument_IdOrderByWorkTypeAsc(documentId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WorkVolumeResponse> listByConstructionObject(Long constructionObjectId, Pageable pageable) {
        return workVolumeRepository.findByConstructionObjectIdOrderByWorkTypeAsc(constructionObjectId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WorkVolumeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Создание ───────────────────────────────────────────────

    @Transactional
    public WorkVolumeResponse create(WorkVolumeRequest request) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new NotFoundException("Document not found: " + request.getDocumentId()));

        WorkVolumeEntity entity = new WorkVolumeEntity(
                request.getConstructionObjectId(),
                request.getConstructionObjectName(),
                document,
                request.getWorkType(),
                request.getVolume(),
                request.getUnit() != null ? request.getUnit() : "шт",
                false, null, null,
                LocalDateTime.now(applicationZoneId)
        );
        return toResponse(workVolumeRepository.save(entity));
    }

    // ─── Обновление ─────────────────────────────────────────────

    @Transactional
    public WorkVolumeResponse update(Long id, WorkVolumeRequest request) {
        WorkVolumeEntity entity = findOrThrow(id);

        entity.setWorkType(request.getWorkType());
        entity.setVolume(request.getVolume());
        entity.setUnit(request.getUnit());
        entity.setConstructionObjectId(request.getConstructionObjectId());
        entity.setConstructionObjectName(request.getConstructionObjectName());

        return toResponse(workVolumeRepository.save(entity));
    }

    // ─── Утверждение ────────────────────────────────────────────

    @Transactional
    public WorkVolumeResponse approve(Long id, Long approvedByUserId) {
        WorkVolumeEntity entity = findOrThrow(id);
        UserEntity approver = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + approvedByUserId));

        entity.setApproved(true);
        entity.setApprovedBy(approver);
        entity.setApprovedAt(LocalDateTime.now(applicationZoneId));

        return toResponse(workVolumeRepository.save(entity));
    }

    // ─── Удаление ───────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        WorkVolumeEntity entity = findOrThrow(id);
        workVolumeRepository.delete(entity);
    }

    // ─── Вспомогательные ────────────────────────────────────────

    WorkVolumeEntity findOrThrow(Long id) {
        return workVolumeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Work volume not found: " + id));
    }

    private DocumentEntity findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));
    }

    private WorkVolumeResponse toResponse(WorkVolumeEntity e) {
        return new WorkVolumeResponse(
                e.getId(),
                e.getConstructionObjectId(),
                e.getConstructionObjectName(),
                e.getDocument().getId(),
                e.getDocument().getDocumentNumber(),
                e.getWorkType(),
                e.getVolume(),
                e.getUnit(),
                e.isApproved(),
                e.getApprovedBy() != null ? e.getApprovedBy().getId() : null,
                e.getApprovedBy() != null ? e.getApprovedBy().getUsername() : null,
                e.getApprovedAt(),
                e.getCreatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}

