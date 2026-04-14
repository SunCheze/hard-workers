package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.AuditLogResponse;
import ru.artwell.contractor.service.AuditLogService;

import java.time.LocalDateTime;

/**
 * REST-контроллер для просмотра журнала аудита (audit_log).
 *
 * Audit log — read-only. Записи создаются автоматически при действиях
 * в системе (создание, обновление, удаление документов, пользователей и т.д.).
 * Пользователи могут только просматривать историю действий.
 *
 * Доступные фильтры: userId, action, entityType, from, to.
 * Все фильтры опциональны — пустые пропускаются.
 *
 * Эндпоинты:
 *   GET /api/audit                             → поиск с фильтрами
 *   GET /api/audit/users/{userId}               → аудит по пользователю
 *   GET /api/audit/entities/{entityType}/{id}  → аудит по сущности
 *   GET /api/audit/{id}                         → детали записи
 */
@Tag(name = "Audit Log", description = "Журнал аудита (read-only): история всех действий в системе")
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    /**
     * Сервис аудита. Помимо чтения, содержит метод log() для записи
     * действий, который вызывается из других сервисов.
     */
    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Поиск по аудиту с множественными фильтрами.
     *
     * Как работает:
     * 1. Принимает до 5 опциональных фильтров: userId, action, entityType, from, to
     * 2. Все параметры опциональны — пустые/null пропускаются
     * 3. Возвращает записи, отсортированные по timestamp DESC (от новых к старым)
     *
     * Пример: GET /api/audit?userId=1&action=CREATE&entityType=Document&from=2024-01-01T00:00:00
     */
    @Operation(summary = "Поиск по аудиту",
            description = "Фильтры: userId, action (CREATE/UPDATE/DELETE), entityType (Document/User/...), from, to")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> search(
            @Parameter(description = "Фильтр по ID пользователя")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Фильтр по действию (CREATE, UPDATE, DELETE, ...)")
            @RequestParam(required = false) String action,

            @Parameter(description = "Фильтр по типу сущности (Document, User, Organization, ...)")
            @RequestParam(required = false) String entityType,

            @Parameter(description = "Начало временного диапазона (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Конец временного диапазона (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditLogService.search(userId, action, entityType, from, to,
                PageRequest.of(page, size)));
    }

    /**
     * Все записи аудита для конкретного пользователя.
     *
     * Пример: GET /api/audit/users/5?page=0&size=20
     * Ответ: все действия пользователя #5, отсортированные по времени.
     */
    @Operation(summary = "Аудит по пользователю",
            description = "Все действия конкретного пользователя, от новых к старым")
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> listByUser(
            @Parameter(description = "ID пользователя")
            @PathVariable Long userId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditLogService.listByUser(userId, PageRequest.of(page, size)));
    }

    /**
     * Все записи аудита для конкретной сущности.
     *
     * Пример: GET /api/audit/entities/Document/5
     * Ответ: все действия с документом #5 (создание, обновления, удаление, смена статуса).
     *
     * Это полезно для восстановления полной истории работы с конкретным объектом.
     */
    @Operation(summary = "Аудит по сущности",
            description = "Все действия с конкретной сущностью (например, Document#5)")
    @GetMapping("/entities/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogResponse>> listByEntity(
            @Parameter(description = "Тип сущности (Document, User, Organization, ...)")
            @PathVariable String entityType,

            @Parameter(description = "ID сущности")
            @PathVariable Long entityId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditLogService.listByEntity(entityType, entityId, PageRequest.of(page, size)));
    }

    /**
     * Детали одной записи аудита.
     */
    @Operation(summary = "Детали записи аудита")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись найдена"),
            @ApiResponse(responseCode = "404", description = "Запись не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getById(
            @Parameter(description = "ID записи аудита")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(auditLogService.getById(id));
    }
}
