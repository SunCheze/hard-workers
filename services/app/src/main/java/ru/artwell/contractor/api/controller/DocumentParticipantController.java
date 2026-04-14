package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.DocumentParticipantRequest;
import ru.artwell.contractor.dto.DocumentParticipantResponse;
import ru.artwell.contractor.service.DocumentParticipantService;

import java.util.List;

/**
 * REST-контроллер для работы с участниками документов.
 *
 * Управляет назначением пользователей на роли в рамках конкретного документа.
 * В отличие от системных ролей (ADMIN, CONTRACTOR и т.д.), роли участников
 * определяют функцию пользователя в рамках конкретного документа.
 *
 * Доступные роли участников:
 *   AUTHOR    — автор документа
 *   REVIEWER  — рецензент / проверяющий
 *   APPROVER  — утверждающее лицо
 *   SIGNER    — подписант
 *   OBSERVER  — наблюдатель (только чтение)
 *   EXECUTOR  — исполнитель по документу
 *
 * Ограничения:
 * — Один пользователь не может иметь две одинаковые роли в одном документе
 * — Пользователь может иметь несколько РАЗНЫХ ролей в одном документе
 *   (например, AUTHOR + SIGNER)
 *
 * Общая схема:
 *   GET    /api/documents/{documentId}/participants                     → список участников
 *   POST   /api/documents/{documentId}/participants                     → назначить участника
 *   DELETE /api/documents/{documentId}/participants/{participantId}     → удалить участника
 */
@Tag(name = "Document Participants", description = "Участники документа: назначение ролей (AUTHOR, REVIEWER, APPROVER, ...)")
@RestController
@RequestMapping("/api/documents/{documentId}/participants")
public class DocumentParticipantController {

    /**
     * Сервис участников документов — содержит бизнес-логику: валидацию ролей,
     * проверку уникальности назначения, маппинг Entity → DTO.
     */
    private final DocumentParticipantService documentParticipantService;

    public DocumentParticipantController(DocumentParticipantService documentParticipantService) {
        this.documentParticipantService = documentParticipantService;
    }

    /**
     * Получить список всех участников документа.
     *
     * Как работает:
     * 1. Проверяет существование документа (404 если не найден)
     * 2. Загружает всех участников документа, отсортированных по роли
     * 3. Каждый участник содержит: данные пользователя и его роль в документе
     *
     * Пример: GET /api/documents/1/participants
     * Ответ: [
     *   { id: 1, userId: 5, username: "ivanov", fullName: "Иванов И.И.",
     *     userRole: "CONTRACTOR", participantRole: "AUTHOR", ... },
     *   { id: 2, userId: 8, username: "petrov", fullName: "Петров П.П.",
     *     userRole: "SUPERVISOR", participantRole: "REVIEWER", ... }
     * ]
     */
    @Operation(summary = "Список участников документа",
            description = "Все участники с их ролями (AUTHOR, REVIEWER, APPROVER, ...)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список участников"),
            @ApiResponse(responseCode = "404", description = "Документ не найден")
    })
    @GetMapping
    public ResponseEntity<List<DocumentParticipantResponse>> list(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(documentParticipantService.listByDocument(documentId));
    }

    /**
     * Назначить участника на документ.
     *
     * Как работает:
     * 1. @Valid проверяет @NotNull userId и @NotBlank participantRole
     * 2. Проверяет существование документа (404) и пользователя (400)
     * 3. Проверяет допустимость participantRole (AUTHOR, REVIEWER, APPROVER, SIGNER, OBSERVER, EXECUTOR)
     * 4. Проверяет, что пользователь ещё не назначен на эту роль в данном документе (400)
     * 5. Создаёт запись и возвращает 201 Created
     *
     * Пример запроса:
     * POST /api/documents/1/participants
     * { "userId": 5, "participantRole": "REVIEWER" }
     */
    @Operation(summary = "Назначить участника на документ")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Участник назначен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации: неверная роль, пользователь не найден, или уже назначен"),
            @ApiResponse(responseCode = "404", description = "Документ не найден")
    })
    @PostMapping
    public ResponseEntity<DocumentParticipantResponse> assign(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Valid @RequestBody DocumentParticipantRequest request
    ) {
        DocumentParticipantResponse created = documentParticipantService.assign(documentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Удалить участника из документа.
     *
     * Как работает:
     * 1. Ищет запись участника по {participantId} с проверкой принадлежности к {documentId}
     *    (404 если не найден или принадлежит другому документу)
     * 2. Физически удаляет запись (участники не являются критичными данными)
     */
    @Operation(summary = "Удалить участника из документа")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участник удалён"),
            @ApiResponse(responseCode = "404", description = "Участник или документ не найден")
    })
    @DeleteMapping("/{participantId}")
    public ResponseEntity<Void> remove(
            @Parameter(description = "ID документа", example = "1")
            @PathVariable Long documentId,

            @Parameter(description = "ID записи участника", example = "3")
            @PathVariable Long participantId
    ) {
        documentParticipantService.remove(documentId, participantId);
        return ResponseEntity.ok().build();
    }
}

