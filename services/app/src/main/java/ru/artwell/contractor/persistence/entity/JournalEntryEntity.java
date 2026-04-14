package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries")
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

    protected JournalEntryEntity() {
    }

    public JournalEntryEntity(DocumentEntity document, String action, String comment,
                              UserEntity performedBy, LocalDateTime performedAt, String journalType) {
        this.document = document;
        this.action = action;
        this.comment = comment;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.journalType = journalType;
    }
    public Long getId() { return id; }
    public DocumentEntity getDocument() { return document; }
    public String getAction() { return action; }
    public String getComment() { return comment; }
    public UserEntity getPerformedBy() { return performedBy; }
    public LocalDateTime getPerformedAt() { return performedAt; }
    public String getJournalType() { return journalType; }
}