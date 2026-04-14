package ru.artwell.contractor.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artwell.contractor.persistence.entity.DocumentExtendedAttributeEntity;

public interface DocumentExtendedAttributeRepository extends JpaRepository<DocumentExtendedAttributeEntity, Long> {
}