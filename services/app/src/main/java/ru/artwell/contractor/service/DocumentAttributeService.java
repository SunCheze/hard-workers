package ru.artwell.contractor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.DocumentAttributeRequest;
import ru.artwell.contractor.dto.DocumentAttributeResponse;
import ru.artwell.contractor.persistence.entity.DocumentVersionEntity;
import ru.artwell.contractor.persistence.entity.DocumentExtendedAttributeEntity;
import ru.artwell.contractor.persistence.repository.DocumentVersionRepository;
import ru.artwell.contractor.persistence.repository.DocumentExtendedAttributeRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DocumentAttributeService {

    private final DocumentExtendedAttributeRepository attributeRepository;
    private final DocumentVersionRepository documentVersionRepository;

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public DocumentAttributeService(DocumentExtendedAttributeRepository attributeRepository,
                                    DocumentVersionRepository documentVersionRepository) {
        this.attributeRepository = attributeRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentAttributeResponse> listByDocument(Long documentId) {
        return attributeRepository.findAll().stream()
                .filter(a -> a.getDocumentVersion() != null
                        && a.getDocumentVersion().getDocument() != null
                        && a.getDocumentVersion().getDocument().getId().equals(documentId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentAttributeResponse create(Long documentId, DocumentAttributeRequest request) {
        DocumentVersionEntity version = documentVersionRepository
                .findTopByDocument_IdOrderByVersionNumberDesc(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        String type = request.getAttributeType() != null ? request.getAttributeType() : "STRING";

        DocumentExtendedAttributeEntity entity = new DocumentExtendedAttributeEntity(
                version, request.getAttributeName(), request.getAttributeValue(),
                type, LocalDateTime.now(ZONE)
        );
        return toResponse(attributeRepository.save(entity));
    }

    @Transactional
    public DocumentAttributeResponse update(Long documentId, Long attributeId, DocumentAttributeRequest request) {
        DocumentExtendedAttributeEntity entity = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new NotFoundException("Attribute not found: " + attributeId));

        entity.setAttributeValue(request.getAttributeValue());
        if (request.getAttributeType() != null) {
            entity.setAttributeType(request.getAttributeType());
        }
        entity.setUpdatedAt(LocalDateTime.now(ZONE));
        return toResponse(attributeRepository.save(entity));
    }

    @Transactional
    public void delete(Long documentId, Long attributeId) {
        attributeRepository.deleteById(attributeId);
    }

    private DocumentAttributeResponse toResponse(DocumentExtendedAttributeEntity e) {
        return new DocumentAttributeResponse(
                e.getId(),
                e.getDocumentVersion() != null ? e.getDocumentVersion().getId() : null,
                e.getAttributeName(),
                e.getAttributeValue(),
                e.getAttributeType(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}