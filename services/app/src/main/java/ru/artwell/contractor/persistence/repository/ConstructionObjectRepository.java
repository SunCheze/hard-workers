package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.ConstructionObjectEntity;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

//Репозиторий для работы с таблицей construction_objects
public interface ConstructionObjectRepository extends JpaRepository<ConstructionObjectEntity, Long> {

    Optional<ConstructionObjectEntity> findByObjectCode(String objectCode);
    Page<ConstructionObjectEntity> findByStatusAndActiveTrue(String status, Pageable pageable);

    Page<ConstructionObjectEntity> findByActiveTrue(Pageable pageable);

    //Поиск объектов, где пользователь является участником в любой из ролей (customer, contractor, designer, supervisor).
    @Query("""
            SELECT o FROM ConstructionObjectEntity o
            WHERE o.active = true
              AND (o.customer.id = :userId
                OR o.contractor.id = :userId
                OR o.designer.id = :userId
                OR o.supervisor.id = :userId)
            """)
    List<ConstructionObjectEntity> findByParticipantUserId(@Param("userId") Long userId);


    //Текстовый поиск по коду или названию объекта.
    @Query("""
            SELECT o FROM ConstructionObjectEntity o
            WHERE o.active = true
              AND (:status IS NULL OR o.status = :status)
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
