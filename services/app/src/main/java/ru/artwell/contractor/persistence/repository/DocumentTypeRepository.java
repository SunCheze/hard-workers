package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.DocumentTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {

    Optional<DocumentTypeEntity> findByTypeCode(String typeCode);

    Page<DocumentTypeEntity> findByActiveTrue(Pageable pageable);

    Page<DocumentTypeEntity> findByCategoryIgnoreCaseAndActiveTrue(String category, Pageable pageable);

    @Query("SELECT dt FROM DocumentTypeEntity dt WHERE (dt.active = true) AND " +
            "(LOWER(dt.typeName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(dt.typeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DocumentTypeEntity> search(@Param("search") String search, Pageable pageable);

    boolean existsByTypeCode(String typeCode);
}
