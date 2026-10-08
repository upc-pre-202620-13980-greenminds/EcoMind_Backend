package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.ProcessedMonetizationRequestPersistenceEntity;

public interface ProcessedMonetizationRequestPersistenceRepository
    extends JpaRepository<ProcessedMonetizationRequestPersistenceEntity, String> {}
