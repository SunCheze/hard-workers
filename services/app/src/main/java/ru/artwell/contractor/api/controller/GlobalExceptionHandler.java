package ru.artwell.contractor.api.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.ErrorResponse;
import ru.artwell.contractor.dto.UploadDocumentResponse;
import ru.artwell.contractor.dto.ValidationErrorDto;
import ru.artwell.contractor.exception.MultipartFileReadException;
import ru.artwell.contractor.persistence.entity.DocumentValidationStatus;
import ru.artwell.contractor.service.DocumentService;
import ru.artwell.contractor.service.XsdCatalogService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ZoneId applicationZoneId;

    public GlobalExceptionHandler(
            @Qualifier(AppTimeConfiguration.APPLICATION_ZONE_ID) ZoneId applicationZoneId) {
        this.applicationZoneId = applicationZoneId;
    }

    @ExceptionHandler(DocumentService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DocumentService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DocumentService.ConflictException.class)
    public ResponseEntity<UploadDocumentResponse> handleConflict(DocumentService.ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(uploadErrorEnvelope(ex.getMessage(), DocumentValidationStatus.INVALID_CONFLICT));
    }

    @ExceptionHandler(XsdCatalogService.UnknownDocumentTypeException.class)
    public ResponseEntity<UploadDocumentResponse> handleUnknownDocumentType(
            XsdCatalogService.UnknownDocumentTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(uploadErrorEnvelope(ex.getMessage(), DocumentValidationStatus.INVALID_UNKNOWN_DOCUMENT_TYPE));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MultipartFileReadException.class)
    public ResponseEntity<ErrorResponse> handleMultipartFileRead(MultipartFileReadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "При загрузке документа произошла ошибка. Проверьте, что файл не повреждён, и попробуйте снова."
        ));
    }
    // ─── Organization ───────────────────────────────────────────

    @ExceptionHandler(OrganizationService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotFound(OrganizationService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── User ───────────────────────────────────────────────────

    @ExceptionHandler(UserService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── Construction Object ────────────────────────────────────

    @ExceptionHandler(ConstructionObjectService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleObjectNotFound(ConstructionObjectService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // ─── Вспомогательные ────────────────────────────────────────

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }

    // ─── Journal Entry ──────────────────────────────────────────

    @ExceptionHandler(JournalEntryService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJournalEntryNotFound(JournalEntryService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    // ─── Work Volume ─────────────────────────────────────────────

    @ExceptionHandler(WorkVolumeService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkVolumeNotFound(WorkVolumeService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    // ─── Permission ──────────────────────────────────────────────

    @ExceptionHandler(RoleDocumentPermissionService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(RoleDocumentPermissionService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    // ─── Audit Log ───────────────────────────────────────────────

    @ExceptionHandler(AuditLogService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuditLogNotFound(AuditLogService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    // ─── Notification ────────────────────────────────────────────

    @ExceptionHandler(NotificationService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(NotificationService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    // ─── Journal Entry ──────────────────────────────────────────

    @ExceptionHandler(JournalEntryService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJournalEntryNotFound(JournalEntryService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Work Volume ─────────────────────────────────────────────

    @ExceptionHandler(WorkVolumeService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkVolumeNotFound(WorkVolumeService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Permission ──────────────────────────────────────────────

    @ExceptionHandler(RoleDocumentPermissionService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(RoleDocumentPermissionService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Audit Log ───────────────────────────────────────────────

    @ExceptionHandler(AuditLogService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuditLogNotFound(AuditLogService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Notification ────────────────────────────────────────────

    @ExceptionHandler(NotificationService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(NotificationService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── User ───────────────────────────────────────────────────

    @ExceptionHandler(UserService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Document Type ──────────────────────────────────────────

    @ExceptionHandler(DocumentTypeService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentTypeNotFound(DocumentTypeService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

// ─── Construction Object ────────────────────────────────────

    @ExceptionHandler(ConstructionObjectService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConstructionObjectNotFound(ConstructionObjectService.NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    private UploadDocumentResponse uploadErrorEnvelope(String message, DocumentValidationStatus status) {
        return new UploadDocumentResponse(
                null,
                null,
                null,
                null,
                0,
                LocalDateTime.now(applicationZoneId),
                false,
                status,
                List.of(new ValidationErrorDto(message, null, null))
        );
    }
}