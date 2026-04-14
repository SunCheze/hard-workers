package ru.artwell.contractor.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.artwell.contractor.persistence.entity.ConstructionObjectEntity;

import java.util.List;
import java.util.Optional;

public interface ConstructionObjectRepository extends JpaRepository<ConstructionObjectEntity, Long> {

    Optional<ConstructionObjectEntity> findByObjectCode(String objectCode);

    Page<ConstructionObjectEntity> findByStatus(String status, Pageable pageable);

    @Query("""
            SELECT o FROM ConstructionObjectEntity o
            WHERE (:status IS NULL OR o.status = :status)
              AND (
                LOWER(o.objectCode) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(o.objectName) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<ConstructionObjectEntity> search(@Param("status") String status,
                                          @Param("query") String query,
                                          Pageable pageable);

    boolean existsByObjectCode(String objectCode);
}