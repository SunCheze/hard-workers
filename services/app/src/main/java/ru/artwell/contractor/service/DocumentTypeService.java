package ru.artwell.contractor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.DocumentTypeRequest;
import ru.artwell.contractor.dto.DocumentTypeResponse;
import ru.artwell.contractor.persistence.entity.DocumentTypeEntity;
import ru.artwell.contractor.persistence.repository.DocumentTypeRepository;

/**
 * Сервис для работы с типами документов (справочник document_types).
 *
 * Типы документов определяют классификацию XML-документов по их корневому
 * элементу. Например: ПОДРЯД, АКТ_ПРИЁМКИ, ПРОЕКТ and т.д.
 * Классификация соответствует реестру Минстроя (21 тип).
 *
 * Каждый тип может иметь XSD-схему для валидации XML.
 */
@Service
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeService(DocumentTypeRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }

    // ─── Создание ───────────────────────────────────────────────

    /**
     * Создаёт новый тип документа.
     * Проверяет уникальность typeCode перед сохранением.
     */
    @Transactional
    public DocumentTypeResponse create(DocumentTypeRequest request) {
        if (documentTypeRepository.existsByTypeCode(request.getTypeCode())) {
            throw new IllegalArgumentException(
                    "Document type with code '" + request.getTypeCode() + "' already exists");
        }

        DocumentTypeEntity entity = new DocumentTypeEntity(
                request.getTypeCode(),
                request.getTypeName(),
                request.getCategory(),
                request.getXsdSchemaPath(),
                true
        );
        DocumentTypeEntity saved = documentTypeRepository.save(entity);
        return toResponse(saved);
    }

    // ─── Чтение ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DocumentTypeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Список типов документов с пагинацией и фильтрами.
     * Поддерживает фильтрацию по категории и текстовый поиск по typeCode/typeName.
     */
    @Transactional(readOnly = true)
    public Page<DocumentTypeResponse> list(String category, String search, Pageable pageable) {
        Page<DocumentTypeEntity> page;

        if (search != null && !search.isBlank()) {
            page = documentTypeRepository.search(search.trim(), pageable);
        } else if (category != null && !category.isBlank()) {
            page = documentTypeRepository.findByCategoryIgnoreCaseAndActiveTrue(category.trim(), pageable);
        } else {
            page = documentTypeRepository.findByActiveTrue(pageable);
        }

        return page.map(this::toResponse);
    }

    // ─── Обновление ─────────────────────────────────────────────

    /**
     * Обновляет данные типа документа.
     * typeCode нельзя менять — он является бизнес-ключом.
     */
    @Transactional
    public DocumentTypeResponse update(Long id, DocumentTypeRequest request) {
        DocumentTypeEntity entity = findOrThrow(id);

        // Если пытаются сменить typeCode на уже существующий — ошибка
        if (!entity.getTypeCode().equals(request.getTypeCode())
                && documentTypeRepository.existsByTypeCode(request.getTypeCode())) {
            throw new IllegalArgumentException(
                    "Document type with code '" + request.getTypeCode() + "' already exists");
        }

        entity.setTypeName(request.getTypeName());
        entity.setCategory(request.getCategory());
        entity.setXsdSchemaPath(request.getXsdSchemaPath());

        return toResponse(documentTypeRepository.save(entity));
    }

    // ─── Удаление (мягкое) ──────────────────────────────────────

    /**
     * Мягкое удаление типа документа: active = false.
     * Тип остаётся в БД для сохранения ссылочной целостности
     * (существующие документы ссылаются на него).
     */
    @Transactional
    public void deactivate(Long id) {
        DocumentTypeEntity entity = findOrThrow(id);
        entity.setActive(false);
        documentTypeRepository.save(entity);
    }

    // ─── Вспомогательные ────────────────────────────────────────

    DocumentTypeEntity findOrThrow(Long id) {
        return documentTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document type not found: " + id));
    }

    private DocumentTypeResponse toResponse(DocumentTypeEntity entity) {
        return new DocumentTypeResponse(
                entity.getId(),
                entity.getTypeCode(),
                entity.getTypeName(),
                entity.getCategory(),
                entity.getXsdSchemaPath(),
                entity.isActive()
        );
    }

    // ─── Исключения ─────────────────────────────────────────────

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}

