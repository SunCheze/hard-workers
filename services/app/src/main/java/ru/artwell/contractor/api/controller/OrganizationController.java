package ru.artwell.contractor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import ru.artwell.contractor.dto.ErrorResponse;
import ru.artwell.contractor.dto.OrganizationRequest;
import ru.artwell.contractor.dto.OrganizationResponse;
import ru.artwell.contractor.service.OrganizationService;

/**
 * REST-контроллер для работы с организациями.
 *
 * Предоставляет CRUD-эндпоинты для справочника юридических лиц:
 * застройщиков, подрядчиков, проектировщиков и организаций строительного контроля.
 *
 * Все эндпоинты возвращают JSON. Удаление — мягкое (isActive = false).
 *
 * Общая схема взаимодействия:
 *   HTTP-запрос → Controller → Service → Repository → БД (H2/PostgreSQL)
 *   Controller получает DTO, передаёт в Service, Service работает с Entity,
 *   маппит Entity → DTO и возвращает в Controller, который оборачивает в ResponseEntity.
 */
@Tag(name = "Organizations", description = "CRUD для справочника организаций (юридических лиц)")
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    /**
     * Сервис организаций — содержит бизнес-логику: валидацию ИНН,
     * проверку уникальности, маппинг Entity ↔ DTO.
     * Контроллер НЕ содержит бизнес-логику, только маршрутизацию HTTP.
     */
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Получить список организаций с пагинацией и фильтрами.
     *
     * Как работает:
     * 1. Извлекает параметры query из URL (?orgType=..., &search=..., &page=..., &size=...)
     * 2. Формирует объект Pageable (номер страницы, размер, сортировка)
     * 3. Передаёт в OrganizationService.list(), который обращается к БД
     * 4. Возвращает Page<OrganizationResponse> с метаданными пагинации
     *
     * Пример: GET /api/organizations?search=Строй&page=0&size=10
     */
    @Operation(summary = "Список организаций", description = "Пагинация, фильтры по типу и текстовый поиск")
    @GetMapping
    public ResponseEntity<Page<OrganizationResponse>> list(
            @Parameter(description = "Фильтр по типу организации (CUSTOMER, CONTRACTOR, ...)")
            @RequestParam(required = false) String orgType,

            @Parameter(description = "Текстовый поиск по названию или ИНН")
            @RequestParam(required = false) String search,

            @Parameter(description = "Показывать только активных")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Номер страницы (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        // PageRequest.of(page, size) создаёт объект пагинации для Spring Data JPA
        // Фреймворк автоматически выполнит COUNT + LIMIT OFFSET запросы к БД
        Page<OrganizationResponse> result = organizationService.list(
                orgType, search, isActive, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    /**
     * Получить одну организацию по ID.
     *
     * Как работает:
     * 1. Извлекает {id} из пути URL
     * 2. Передаёт в OrganizationService.getById()
     * 3. Если не найдена — сервис выбросит NotFoundException → GlobalExceptionHandler → 404
     */
    @Operation(summary = "Детали организации по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация найдена",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(
            @Parameter(description = "ID организации", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    /**
     * Создать новую организацию.
     *
     * Как работает:
     * 1. @Valid автоматически проверяет @NotBlank поля в OrganizationRequest
     *    (если orgName пустой → 400 Bad Request до попадания в контроллер)
     * 2. Контроллер передаёт DTO в OrganizationService.create()
     * 3. Сервис валидирует ИНН (формат 10/12 цифр + уникальность), сохраняет в БД
     * 4. Возвращает 201 Created с сохранённой организацией
     */
    @Operation(summary = "Создать организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Организация создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации (ИНН, дубликат)")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные организации")
            @Valid @RequestBody OrganizationRequest request
    ) {
        OrganizationResponse created = organizationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить данные организации.
     *
     * Как работает:
     * 1. Находит организацию по ID (404 если не найдена)
     * 2. Проверяет ИНН на уникальность (если изменился)
     * 3. Обновляет все поля и возвращает обновлённую запись
     */
    @Operation(summary = "Обновить организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация обновлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request
    ) {
        return ResponseEntity.ok(organizationService.update(id, request));
    }

    /**
     * Мягкое удаление организации (isActive → false).
     *
     * Как работает:
     * Организация НЕ удаляется из БД физически — это нарушило бы
     * ссылочную целостность (пользователи, объекты ссылаются на неё).
     * Вместо этого устанавливается isActive = false, и организация
     * перестаёт появляться в стандартных списках.
     */
    @Operation(summary = "Удалить организацию (мягкое)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация деактивирована"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizationService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}