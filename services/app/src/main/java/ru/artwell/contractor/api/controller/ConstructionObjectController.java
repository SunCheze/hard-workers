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
import ru.artwell.contractor.dto.ConstructionObjectDetailResponse;
import ru.artwell.contractor.dto.ConstructionObjectRequest;
import ru.artwell.contractor.dto.ConstructionObjectResponse;
import ru.artwell.contractor.service.ConstructionObjectService;

/**
 * REST-контроллер для работы с объектами капитального строительства.
 *
 * Предоставляет CRUD-эндпоинты для управления объектами:
 * здания, сооружения и иные объекты с привязкой к участникам проекта.
 *
 * Особенности:
 * — При создании/обновлении указываются ID пользователей-участников
 *   (customer, contractor, designer, supervisor).
 * — GET по ID возвращает детальную информацию с участниками и их организациями.
 * — GET список возвращает краткую информацию (без участников — для производительности).
 * — Удаление мягкое (status → "deleted").
 *
 * Общая схема:
 *   POST   /api/construction-objects          → создать объект
 *   GET    /api/construction-objects          → список (фильтры: status, search)
 *   GET    /api/construction-objects/{id}     → детали с участниками
 *   PUT    /api/construction-objects/{id}     → обновить
 *   DELETE /api/construction-objects/{id}     → удалить (мягкое)
 */
@Tag(name = "Construction Objects",
        description = "Объекты капитального строительства с привязкой к участникам проекта")
@RestController
@RequestMapping("/api/construction-objects")
public class ConstructionObjectController {

    private final ConstructionObjectService objectService;

    public ConstructionObjectController(ConstructionObjectService objectService) {
        this.objectService = objectService;
    }

    /**
     * Список объектов строительства с пагинацией и фильтрами.
     *
     * Как работает:
     * 1. Извлекает параметры из URL: ?status=active&search=Панорама&page=0&size=20
     * 2. Если передан search — ConstructionObjectService.search() выполняет
     *    JPQL-запрос с LIKE по полям objectCode и objectName
     * 3. Если передан только status — фильтрация по полю status
     * 4. Возвращает Page<ConstructionObjectResponse> — краткую информацию
     *    (без участников для производительности)
     *
     * Пример: GET /api/construction-objects?search=ЖК&page=0&size=10
     */
    @Operation(summary = "Список объектов строительства",
            description = "Пагинация, фильтры по статусу и текстовый поиск. Без участников.")
    @GetMapping
    public ResponseEntity<Page<ConstructionObjectResponse>> list(
            @Parameter(description = "Фильтр по статусу (active, completed, suspended)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Текстовый поиск по коду или названию объекта")
            @RequestParam(required = false) String search,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ConstructionObjectResponse> result = objectService.list(
                status, search, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    /**
     * Получить детали объекта строительства по ID.
     *
     * Как работает:
     * 1. Загружает ConstructionObjectEntity из БД
     * 2. Для каждого участника (customer, contractor, designer, supervisor)
     *    загружает связанного UserEntity и его OrganizationEntity
     * 3. Формирует ConstructionObjectDetailResponse с вложенными ParticipantInfo:
     *    { userId, username, orgName }
     *
     * Если участник не назначен — соответствующее поле = null.
     */
    @Operation(summary = "Детали объекта с участниками проекта",
            description = "Включает информацию о заказчике, подрядчике, проектировщике и стройконтроле")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Детали объекта",
                    content = @Content(schema = @Schema(implementation = ConstructionObjectDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Объект не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConstructionObjectDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(objectService.getById(id));
    }

    /**
     * Создать объект строительства.
     *
     * Как работает:
     * 1. @Valid проверяет @NotBlank поля (objectCode, objectName)
     * 2. ConstructionObjectService.create():
     *    — проверяет уникальность objectCode
     *    — загружает участников по ID (404 если пользователь не найден)
     *    — создаёт ConstructionObjectEntity со всеми связями
     * 3. Возвращает 201 с полными деталями созданного объекта
     *
     * Пример тела запроса:
     * {
     *   "objectCode": "OBJ-001",
     *   "objectName": "ЖК Панорама, корпус 1",
     *   "address": "г. Москва, ул. Строителей, 10",
     *   "customerUserId": 1,
     *   "contractorUserId": 2
     * }
     */
    @Operation(summary = "Создать объект строительства")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Объект создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или дубликат кода")
    })
    @PostMapping
    public ResponseEntity<ConstructionObjectDetailResponse> create(
            @Valid @RequestBody ConstructionObjectRequest request
    ) {
        ConstructionObjectDetailResponse created = objectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить объект строительства.
     *
     * Можно обновить любые поля, включая замену участников проекта.
     * Если objectCode изменился — проверяется уникальность нового значения.
     */
    @Operation(summary = "Обновить объект строительства",
            description = "Можно менять данные и заменять участников")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Объект обновлён"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Объект или пользователь не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConstructionObjectDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ConstructionObjectRequest request
    ) {
        return ResponseEntity.ok(objectService.update(id, request));
    }

    /**
     * Мягкое удаление объекта (status → "deleted").
     *
     * Объект остаётся в БД — к нему привязаны документы через
     * DocumentEntity.constructionObject, и физическое удаление
     * нарушило бы ссылочную целостность.
     */
    @Operation(summary = "Удалить объект (мягкое)", description = "status → 'deleted'")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Объект удалён"),
            @ApiResponse(responseCode = "404", description = "Объект не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        objectService.delete(id);
        return ResponseEntity.ok().build();
    }
}