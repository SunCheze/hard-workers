package services.app.src.main.java.ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artwell.contractor.dto.NotificationCreateRequest;
import ru.artwell.contractor.dto.NotificationResponse;
import ru.artwell.contractor.service.NotificationService;

/**
 * REST-контроллер для работы с уведомлениями пользователей (notifications).
 *
 * Предоставляет:
 * — просмотр уведомлений (все / только непрочитанные);
 * — создание уведомлений;
 * — отметку одного или всех уведомлений как прочитанные;
 * — подсчёт непрочитанных.
 *
 * Эндпоинты:
 *   GET    /api/users/{userId}/notifications              → все уведомления
 *   GET    /api/users/{userId}/notifications/unread      → только непрочитанные
 *   GET    /api/users/{userId}/notifications/unread-count → количество непрочитанных
 *   POST   /api/notifications                            → создать уведомление
 *   PATCH  /api/users/{userId}/notifications/{id}/read   → отметить как прочитанное
 *   PATCH  /api/users/{userId}/notifications/read-all    → отметить все как прочитанные
 */
@Tag(name = "Notifications", description = "Уведомления пользователей: просмотр, создание, отметка прочитанными")
@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Все уведомления пользователя")
    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<Page<NotificationResponse>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.listByUser(userId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Непрочитанные уведомления")
    @GetMapping("/users/{userId}/notifications/unread")
    public ResponseEntity<Page<NotificationResponse>> listUnread(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.listUnreadByUser(userId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Количество непрочитанных уведомлений")
    @GetMapping("/users/{userId}/notifications/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @Operation(summary = "Создать уведомление")
    @ApiResponses({ @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "400") })
    @PostMapping("/notifications")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @Operation(summary = "Отметить уведомление как прочитанное")
    @PatchMapping("/users/{userId}/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(userId, id));
    }

    @Operation(summary = "Отметить ВСЕ уведомления как прочитанные",
            description = "Помечает все непрочитанные уведомления пользователя как прочитанные")
    @PatchMapping("/users/{userId}/notifications/read-all")
    public ResponseEntity<Long> markAllAsRead(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }
}