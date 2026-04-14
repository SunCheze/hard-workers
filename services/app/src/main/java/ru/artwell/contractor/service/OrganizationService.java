package ru.artwell.contractor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.dto.OrganizationRequest;
import ru.artwell.contractor.dto.OrganizationResponse;
import ru.artwell.contractor.persistence.entity.OrganizationEntity;
import ru.artwell.contractor.persistence.repository.OrganizationRepository;

/**
 * Сервис для работы с организациями.
 *
 * Обеспечивает CRUD-операции над организациями с валидацией:
 * — проверка уникальности ИНН при создании;
 * — проверка формата ИНН (10 или 12 цифр);
 * — мягкое удаление (isActive = false).
 */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    // ─── Создание ───────────────────────────────────────────────

    /**
     * Создаёт новую организацию.
     *
     * Валидирует ИНН на уникальность и формат перед сохранением.
     *
     * @param request DTO с данными организации
     * @return сохранённая организация в виде DTO
     * @throws IllegalArgumentException если ИНН уже существует или формат неверный
     */
    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        validateInn(request.getInn(), null);

        OrganizationEntity entity = new OrganizationEntity(
                request.getOrgName(),
                request.getOrgShortName(),
                request.getOrgType(),
                request.getInn(),
                request.getKpp(),
                request.getLegalAddress(),
                true
        );
        OrganizationEntity saved = organizationRepository.save(entity);
        return toResponse(saved);
    }

    // ─── Чтение ─────────────────────────────────────────────────

    /** Получить одну организацию по ID */
    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        OrganizationEntity entity = findOrThrow(id);
        return toResponse(entity);
    }

    /** Список организаций с пагинацией и фильтрами */
    @Transactional(readOnly = true)
    public Page<OrganizationResponse> list(String orgType, String search, Boolean isActive, Pageable pageable) {
        Page<OrganizationEntity> page;

        if (search != null && !search.isBlank()) {
            // Текстовый поиск по названию/ИНН
            page = organizationRepository
                    .findByOrgNameContainingIgnoreCaseOrInnContainingIgnoreCaseAndActiveTrue(search, search, pageable);
        } else if (orgType != null && !orgType.isBlank()) {
            page = organizationRepository.findByOrgTypeAndActiveTrue(orgType, pageable);
        } else if (Boolean.TRUE.equals(isActive)) {
            page = organizationRepository.findByActiveTrue(pageable);
        } else {
            page = organizationRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    // ─── Обновление ─────────────────────────────────────────────

    /**
     * Обновляет данные организации.
     * Если ИНН изменился — проверяет уникальность нового значения.
     */
    @Transactional
    public OrganizationResponse update(Long id, OrganizationRequest request) {
        OrganizationEntity entity = findOrThrow(id);
        validateInn(request.getInn(), entity.getId());

        entity.setOrgName(request.getOrgName());
        entity.setOrgShortName(request.getOrgShortName());
        entity.setOrgType(request.getOrgType());
        entity.setInn(request.getInn());
        entity.setKpp(request.getKpp());
        entity.setLegalAddress(request.getLegalAddress());

        return toResponse(organizationRepository.save(entity));
    }

    // ─── Удаление (мягкое) ──────────────────────────────────────

    /**
     * Мягкое удаление: устанавливает isActive = false.
     * Организация остаётся в БД для сохранения ссылочной целостности.
     */
    @Transactional
    public void deactivate(Long id) {
        OrganizationEntity entity = findOrThrow(id);
        entity.setActive(false);
        organizationRepository.save(entity);
    }

    // ─── Вспомогательные методы ──────────────────────────────────

    /** Ищет организацию по ID или выбрасывает исключение */
    OrganizationEntity findOrThrow(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + id));
    }

    /** Валидирует ИНН: формат (10 или 12 цифр) и уникальность */
    private void validateInn(String inn, Long excludeId) {
        if (inn == null || inn.isBlank()) {
            return; // ИНН необязателен
        }
        if (!inn.matches("\\d{10}|\\d{12}")) {
            throw new IllegalArgumentException("ИНН должен содержать 10 или 12 цифр, получено: " + inn);
        }
        // Проверяем уникальность, исключая текущую запись при обновлении
        boolean exists;
        if (excludeId != null) {
            // При обновлении: проверяем, что ИНН не занят другой организацией
            exists = organizationRepository.findByInn(inn)
                    .map(existing -> !existing.getId().equals(excludeId))
                    .orElse(false);
        } else {
            exists = organizationRepository.existsByInn(inn);
        }
        if (exists) {
            throw new IllegalArgumentException("Organization with INN " + inn + " already exists");
        }
    }

    /** Маппинг Entity → DTO */
    private OrganizationResponse toResponse(OrganizationEntity entity) {
        return new OrganizationResponse(
                entity.getId(),
                entity.getOrgName(),
                entity.getOrgShortName(),
                entity.getOrgType(),
                entity.getInn(),
                entity.getKpp(),
                entity.getLegalAddress(),
                entity.isActive()
        );
    }

    // ─── Исключения ─────────────────────────────────────────────

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
