package ru.artwell.contractor.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.config.AppTimeConfiguration;
import ru.artwell.contractor.dto.ConstructionObjectDetailResponse;
import ru.artwell.contractor.dto.ConstructionObjectRequest;
import ru.artwell.contractor.dto.ConstructionObjectResponse;
import ru.artwell.contractor.persistence.entity.ConstructionObjectEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.ConstructionObjectRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;

import java.time.ZoneId;

/**
 * Сервис для работы с объектами капитального строительства.
 *
 * Обеспечивает CRUD-операции над объектами с управлением участниками:
 * — при создании/обновлении привязывает пользователей-участников
 *   (customer, contractor, designer, supervisor);
 * — при чтении деталей собирает информацию об участниках и их организациях;
 * — мягкое удаление (status → "deleted").
 *
 * Объект строительства — это физическое место ведения работ:
 * здание, сооружение или комплекс, к которому привязаны документы.
 */
@Service
public class ConstructionObjectService {

    private final ConstructionObjectRepository objectRepository;
    private final UserRepository userRepository;

    public ConstructionObjectService(ConstructionObjectRepository objectRepository,
                                     UserRepository userRepository) {
        this.objectRepository = objectRepository;
        this.userRepository = userRepository;
    }

    // ─── Создание ───────────────────────────────────────────────

    /**
     * Создаёт новый объект строительства.
     *
     * Проверяет уникальность objectCode, валидирует ID участников
     * и создаёт запись с привязкой к пользователям.
     */
    @Transactional
    public ConstructionObjectDetailResponse create(ConstructionObjectRequest request) {
        validateObjectCode(request.getObjectCode(), null);

        ConstructionObjectEntity entity = new ConstructionObjectEntity(
                request.getObjectCode(),
                request.getObjectName(),
                request.getAddress(),
                resolveUser(request.getCustomerUserId()),
                resolveUser(request.getContractorUserId()),
                resolveUser(request.getDesignerUserId()),
                resolveUser(request.getSupervisorUserId()),
                request.getStatus() != null ? request.getStatus() : "active"
        );

        if (request.getStartDate() != null) {
            entity.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            entity.setEndDate(request.getEndDate());
        }

        ConstructionObjectEntity saved = objectRepository.save(entity);
        return toDetailResponse(saved);
    }

    // ─── Чтение ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ConstructionObjectDetailResponse getById(Long id) {
        return toDetailResponse(findOrThrow(id));
    }

    /**
     * Список объектов с пагинацией и фильтрами.
     * Фильтры: status, search (по коду или названию).
     */
    @Transactional(readOnly = true)
    public Page<ConstructionObjectResponse> list(String status, String search, Pageable pageable) {
        Page<ConstructionObjectEntity> page;

        if (search != null && !search.isBlank()) {
            page = objectRepository.search(status, search.trim(), pageable);
        } else if (status != null && !status.isBlank()) {
            page = objectRepository.findByStatus(status, pageable);
        } else {
            page = objectRepository.findByStatus("active", pageable);
        }

        return page.map(this::toListResponse);
    }

    // ─── Обновление ─────────────────────────────────────────────

    /** Обновляет данные объекта и заменяет участников проекта */
    @Transactional
    public ConstructionObjectDetailResponse update(Long id, ConstructionObjectRequest request) {
        ConstructionObjectEntity entity = findOrThrow(id);

        if (request.getObjectCode() != null) {
            validateObjectCode(request.getObjectCode(), entity.getId());
            entity.setObjectCode(request.getObjectCode());
        }
        if (request.getObjectName() != null) {
            entity.setObjectName(request.getObjectName());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            entity.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            entity.setEndDate(request.getEndDate());
        }

        // Обновляем участников (если ID переданы в запросе)
        if (request.getCustomerUserId() != null) {
            entity.setCustomer(resolveUser(request.getCustomerUserId()));
        }
        if (request.getContractorUserId() != null) {
            entity.setContractor(resolveUser(request.getContractorUserId()));
        }
        if (request.getDesignerUserId() != null) {
            entity.setDesigner(resolveUser(request.getDesignerUserId()));
        }
        if (request.getSupervisorUserId() != null) {
            entity.setSupervisor(resolveUser(request.getSupervisorUserId()));
        }

        return toDetailResponse(objectRepository.save(entity));
    }

    // ─── Удаление (мягкое) ──────────────────────────────────────

    /** Устанавливает status = "deleted". Объект остаётся в БД для ссылочной целостности. */
    @Transactional
    public void delete(Long id) {
        ConstructionObjectEntity entity = findOrThrow(id);
        entity.setStatus("deleted");
        objectRepository.save(entity);
    }

    // ─── Вспомогательные методы ──────────────────────────────────

    ConstructionObjectEntity findOrThrow(Long id) {
        return objectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Construction object not found: " + id));
    }

    /** Загружает пользователя по ID или возвращает null */
    private UserEntity resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /** Проверяет уникальность objectCode */
    private void validateObjectCode(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("objectCode is required");
        }
        if (excludeId != null) {
            objectRepository.findByObjectCode(code).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new IllegalArgumentException("Object with code '" + code + "' already exists");
                }
            });
        } else {
            if (objectRepository.existsByObjectCode(code)) {
                throw new IllegalArgumentException("Object with code '" + code + "' already exists");
            }
        }
    }

    /** Маппинг Entity → краткий DTO для списков */
    private ConstructionObjectResponse toListResponse(ConstructionObjectEntity entity) {
        return new ConstructionObjectResponse(
                entity.getId(),
                entity.getObjectCode(),
                entity.getObjectName(),
                entity.getAddress(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    /**
     * Маппинг Entity → детальный DTO с участниками.
     *
     * Для каждого участника (customer, contractor, designer, supervisor)
     * формируется вложенный ParticipantInfo: ID, username и название организации.
     * Если участник не назначен — поле остаётся null.
     */
    private ConstructionObjectDetailResponse toDetailResponse(ConstructionObjectEntity entity) {
        return new ConstructionObjectDetailResponse(
                entity.getId(),
                entity.getObjectCode(),
                entity.getObjectName(),
                entity.getAddress(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                toParticipantInfo(entity.getCustomer()),
                toParticipantInfo(entity.getContractor()),
                toParticipantInfo(entity.getDesigner()),
                toParticipantInfo(entity.getSupervisor())
        );
    }

    /** Маппинг UserEntity → ParticipantInfo (вложенный DTO) */
    private ConstructionObjectDetailResponse.ParticipantInfo toParticipantInfo(UserEntity user) {
        if (user == null) {
            return null;
        }
        String orgName = (user.getOrganization() != null) ? user.getOrganization().getOrgName() : null;
        return new ConstructionObjectDetailResponse.ParticipantInfo(
                user.getId(), user.getUsername(), orgName
        );
    }

    // ─── Исключения ─────────────────────────────────────────────

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}