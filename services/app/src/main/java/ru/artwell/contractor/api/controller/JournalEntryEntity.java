package services.app.src.main.java.ru.artwell.contractor.api.controller;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries", indexes = {
        @Index(name = "idx_journal_document", columnList = "document_id"),
        @Index(name = "idx_journal_user", columnList = "performed_by"),
        @Index(name = "idx_journal_timestamp", columnList = "performed_at")
})
public class JournalEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "action", nullable = false, length = 128)
    private String action;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by", nullable = false)
    private UserEntity performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "journal_type", length = 64)
    private String journalType;

    protected JournalEntryEntity() {}

    public JournalEntryEntity(DocumentEntity document, String action, String comment,
                              UserEntity performedBy, LocalDateTime performedAt, String journalType) {
        this.document = document;
        this.action = action;
        this.comment = comment;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.journalType = journalType;
    }

    // getters...
}
