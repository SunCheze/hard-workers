package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doc_participant_doc_user_role",
                columnNames = {"document_id", "user_id", "participant_role"}
        ),
        indexes = {
                @Index(name = "idx_doc_participants_document", columnList = "document_id"),
                @Index(name = "idx_doc_participants_user", columnList = "user_id")
        }
)
public class DocumentParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersionEntity documentVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "participant_role", length = 256)
    private String participantRole;

    @Column(name = "participant_type", length = 32)
    private String participantType;

    @Column(name = "participant_name", length = 512)
    private String participantName;

    @Column(name = "participant_inn", length = 32)
    private String participantInn;

    @Column(name = "participant_kpp", length = 32)
    private String participantKpp;

    protected DocumentParticipantEntity() {
    }

    public DocumentParticipantEntity(DocumentEntity document, UserEntity user,
                                     String participantRole, LocalDateTime assignedAt) {
        this.document = document;
        this.user = user;
        this.participantRole = participantRole;
        this.assignedAt = assignedAt;
    }

    // ─── Геттеры ───

    public Long getId() { return id; }
    public DocumentEntity getDocument() { return document; }
    public UserEntity getUser() { return user; }
    public String getParticipantRole() { return participantRole; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
}

}
