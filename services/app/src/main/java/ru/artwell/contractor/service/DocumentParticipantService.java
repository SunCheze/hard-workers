package ru.artwell.contractor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.DocumentParticipantRequest;
import ru.artwell.contractor.dto.DocumentParticipantResponse;
import ru.artwell.contractor.persistence.entity.DocumentVersionEntity;
import ru.artwell.contractor.persistence.entity.DocumentParticipantEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.DocumentVersionRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;
import ru.artwell.contractor.persistence.repository.DocumentParticipantRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DocumentParticipantService {

    private final DocumentParticipantRepository participantRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public DocumentParticipantService(DocumentParticipantRepository participantRepository,
                                      DocumentVersionRepository documentVersionRepository,
                                      UserRepository userRepository) {
        this.participantRepository = participantRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentParticipantResponse> listByDocument(Long documentId) {
        return participantRepository.findAll().stream()
                .filter(p -> p.getDocumentVersion() != null
                        && p.getDocumentVersion().getDocument() != null
                        && p.getDocumentVersion().getDocument().getId().equals(documentId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentParticipantResponse assign(Long documentId, DocumentParticipantRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        DocumentVersionEntity version = documentVersionRepository
                .findTopByDocument_IdOrderByVersionNumberDesc(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found: " + documentId));

        DocumentParticipantEntity entity = new DocumentParticipantEntity(
                version, user, request.getParticipantRole(), LocalDateTime.now(ZONE)
        );
        return toResponse(participantRepository.save(entity));
    }

    @Transactional
    public void remove(Long documentId, Long participantId) {
        participantRepository.deleteById(participantId);
    }

    private DocumentParticipantResponse toResponse(DocumentParticipantEntity e) {
        return new DocumentParticipantResponse(
                e.getId(),
                e.getUser().getId(),
                e.getUser().getUsername(),
                e.getUser().getFullName(),
                e.getUser().getRole(),
                e.getParticipantRole(),
                e.getAssignedAt()
        );
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }
}