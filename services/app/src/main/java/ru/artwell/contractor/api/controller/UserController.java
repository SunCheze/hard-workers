package ru.artwell.contractor.api.controller;

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
import ru.artwell.contractor.dto.*;
import ru.artwell.contractor.service.UserService;

import java.util.List;

/**
 * REST-контроллер для работы с пользователями и ролями.
 *
 * Предоставляет:
 * — CRUD пользователей (создание, список, профиль, обновление);
 * — смену роли с записью в audit (role_assignment_history);
 * — деактивацию пользователя;
 * — просмотр истории смены ролей.
 *
 * Пароль хранится в виде BCrypt-хэша и никогда не возвращается в ответах.
 *
 * Общая схема:
 *   POST   /api/users               → создание
 *   GET    /api/users               → список (фльтры: role, organizationId, search)
 *   GET    /api/users/{id}          → профиль
 *   PUT    /api/users/{id}          → обновление (без роли и пароля)
 *   PATCH  /api/users/{id}/role     → смена роли
 *   PATCH  /api/users/{id}/deactivate → деактивация
 *   GET    /api/users/{id}/role-history → история смены ролей
 */
@Tag(name = "Users", description = "Пользователи, роли (7 типов) и история назначений")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Список пользователей с фильтрами и пагинацией.
     *
     * Как работает:
     * 1. Параметры извлекаются из URL: ?role=CONTRACTOR&organizationId=1&search=иван&page=0&size=20
     * 2. Если передан search — UserService.search() выполняет JPQL-запрос с LIKE
     *    по полям username, fullName, email
     * 3. Если передан только role — фильтрация по полю role
     * 4. Возвращает Page<UserResponse> — Spring Data автоматически добавляет
     *    metadata: totalPages, totalElements, number, size
     */
    @Operation(summary = "Список пользователей",
            description = "Фильтры: role, organizationId, search. Поддерживает пагинацию.")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(
            @Parameter(description = "Фильтр по роли (ADMIN, CUSTOMER, CONTRACTOR, ...)")
            @RequestParam(required = false) String role,

            @Parameter(description = "Фильтр по ID организации")
            @RequestParam(required = false) Long organizationId,

            @Parameter(description = "Текстовый поиск по username, fullName, email")
            @RequestParam(required = false) String search,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<UserResponse> result = userService.list(role, organizationId, search, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    /**
     * Создать нового пользователя.
     *
     * Как работает:
     * 1. @Valid проверяет @NotBlank поля (username, password)
     * 2. UserService.create() проверяет:
     *    — уникальность username (409 если занят)
     *    — допустимость роли (если передана)
     *    — существование organizationId
     * 3. Пароль хэшируется BCrypt перед сохранением
     * 4. Возвращает 201 с профилем пользователя (без пароля!)
     */
    @Operation(summary = "Создать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или неверная роль")
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Получить профиль пользователя по ID.
     *
     * В ответе есть вложенный объект organization с данными организации,
     * к которой привязан пользователь (или null если не привязан).
     */
    @Operation(summary = "Профиль пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * Обновить профиль (без смены роли и пароля).
     *
     * Можно изменить: fullName, email, organizationId.
     * Для смены роли используй PATCH /api/users/{id}/role.
     */
    @Operation(summary = "Обновить профиль (без роли и пароля)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    /**
     * Сменить роль пользователя.
     *
     * Как работает:
     * 1. Проверяет, что новая роль входит в список допустимых (7 ролей)
     * 2. Записывает текущую роль как "старую" в role_assignment_history
     * 3. Обновляет role в users
     * 4. Запись в истории содержит: старую роль, новую, кто изменил, когда
     *
     * Это позволяет audit-отслеживать кто и когда менял роли.
     */
    @Operation(summary = "Сменить роль пользователя",
            description = "Записывает старую роль в role_assignment_history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль обновлена"),
            @ApiResponse(responseCode = "400", description = "Недопустимая роль"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleChangeRequest request
    ) {
        // TODO: извлечь username текущего пользователя из SecurityContext
        // сейчас передаём "system" как плейсхолдер
        UserResponse updated = userService.changeRole(id, request, "system");
        return ResponseEntity.ok(updated);
    }

    /**
     * Деактивация пользователя (мягкое удаление).
     *
     * Пользователь остаётся в БД (для ссылочной целостности),
     * но не может авторизоваться (active = false).
     */
    @Operation(summary = "Деактивировать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь деактивирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    /**
     * История смены ролей пользователя.
     *
     * Возвращает список записей от новых к старым.
     * Каждая запись содержит: старую роль, новую, кто изменил, когда.
     */
    @Operation(summary = "История смены ролей",
            description = "Все записи role_assignment_history для данного пользователя")
    @GetMapping("/{id}/role-history")
    public ResponseEntity<List<RoleAssignmentRecordResponse>> getRoleHistory(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getRoleHistory(id));
    }
}
