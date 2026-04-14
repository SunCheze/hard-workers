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
import ru.artwell.contractor.service.*;

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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(OrganizationService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotFound(OrganizationService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConstructionObjectService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleObjectNotFound(ConstructionObjectService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(JournalEntryService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleJournalEntryNotFound(JournalEntryService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WorkVolumeService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkVolumeNotFound(WorkVolumeService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoleDocumentPermissionService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(RoleDocumentPermissionService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AuditLogService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuditLogNotFound(AuditLogService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NotificationService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(NotificationService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DocumentTypeService.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentTypeNotFound(DocumentTypeService.NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
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