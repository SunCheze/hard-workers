package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с таблицей organizations.
 * Предоставляет стандартные CRUD-операции через JpaRepository
 * и методы поиска по полям: тип, ИНН, признак активности, текстовый поиск.
 */
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    /** Поиск организации по ИНН (уникальное поле) */
    Optional<OrganizationEntity> findByInn(String inn);

    /** Все организации заданного типа (CUSTOMER, CONTRACTOR и т.д.) */
    List<OrganizationEntity> findByOrgTypeAndActiveTrue(String orgType);

    /** Поиск по типу с пагинацией */
    Page<OrganizationEntity> findByOrgTypeAndActiveTrue(String orgType, Pageable pageable);

    /** Все активные организации с пагинацией */
    Page<OrganizationEntity> findByActiveTrue(Pageable pageable);

    Page<OrganizationEntity> findByOrgNameContainingIgnoreCaseOrInnContainingIgnoreCaseAndActiveTrue(
            String nameQuery, String innQuery, Pageable pageable);

    /** Проверка существования ИНН (для валидации при создании) */
    boolean existsByInn(String inn);
}
