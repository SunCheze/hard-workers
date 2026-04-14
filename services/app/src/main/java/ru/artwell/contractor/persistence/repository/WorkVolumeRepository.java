package services.app.src.main.java.ru.artwell.contractor.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.WorkVolumeEntity;

import java.util.Optional;

public interface WorkVolumeRepository extends JpaRepository<WorkVolumeEntity, Long> {

    Page<WorkVolumeEntity> findByDocument_IdOrderByWorkTypeAsc(Long documentId, Pageable pageable);

    Page<WorkVolumeEntity> findByConstructionObjectIdOrderByWorkTypeAsc(Long constructionObjectId, Pageable pageable);

    Optional<WorkVolumeEntity> findByIdAndDocument_Id(Long id, Long documentId);
}
