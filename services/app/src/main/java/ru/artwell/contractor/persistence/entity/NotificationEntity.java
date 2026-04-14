package ru.artwell.contractor.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "notification_type", length = 64)
    private String notificationType;

    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean readByUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(UserEntity user, String title, String message,
                              String notificationType, boolean readByUser,
                              LocalDateTime createdAt, LocalDateTime readAt) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.readByUser = readByUser;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getType() { return notificationType; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return readByUser; }
    public boolean isReadByUser() { return readByUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }

    public void setRead(boolean read) { this.readByUser = read; }
    public void setReadByUser(boolean readByUser) { this.readByUser = readByUser; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
