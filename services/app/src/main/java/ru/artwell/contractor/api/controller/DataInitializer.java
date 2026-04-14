package ru.artwell.contractor.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.artwell.contractor.persistence.entity.DocumentTypeEntity;
import ru.artwell.contractor.persistence.entity.OrganizationEntity;
import ru.artwell.contractor.persistence.entity.UserEntity;
import ru.artwell.contractor.persistence.repository.DocumentTypeRepository;
import ru.artwell.contractor.persistence.repository.OrganizationRepository;
import ru.artwell.contractor.persistence.repository.UserRepository;

/**
 * Инициализация справочных данных при старте приложения.
 * Создаёт тестовую организацию и пользователя, если их нет.
 */
@Component
@Order(0)
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DocumentTypeRepository documentTypeRepository;

    private final String testUsername;
    private final String testPassword;

    public DataInitializer(UserRepository userRepository,
                           OrganizationRepository organizationRepository,
                           DocumentTypeRepository documentTypeRepository,
                           @Value("${app.seed.test-username}") String testUsername,
                           @Value("${app.seed.test-password}") String testPassword) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.testUsername = testUsername;
        this.testPassword = testPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        OrganizationEntity org = ensureTestOrganization();
        ensureTestUser(org);
        ensureUnknownDocumentType();
    }

    private OrganizationEntity ensureTestOrganization() {
        return organizationRepository.findAll().stream()
                .filter(o -> o.isActive())
                .findFirst()
                .orElseGet(() -> organizationRepository.save(new OrganizationEntity(
                        "ООО «Артвелл» (тестовая организация)",
                        "Артвелл",
                        "CUSTOMER",
                        "0000000000",
                        null,
                        null,
                        true
                )));
    }

    private void ensureTestUser(OrganizationEntity organization) {
        if (userRepository.findByUsername(testUsername).isEmpty()) {
            String hash = new BCryptPasswordEncoder().encode(testPassword);
            userRepository.save(new UserEntity(
                    testUsername,
                    hash,
                    "Тестовый пользователь",
                    "ADMIN",
                    organization,
                    "test@example.local",
                    true
            ));
        }
    }

    private void ensureUnknownDocumentType() {
        if (documentTypeRepository.findByTypeCode("UNKNOWN").isEmpty()) {
            documentTypeRepository.save(new DocumentTypeEntity(
                    "UNKNOWN",
                    "Unknown document type",
                    "System",
                    null,
                    true
            ));
        }
    }
}
