package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.FamilyScorePersistenceEntity;

public interface FamilyScorePersistenceRepository
        extends JpaRepository<FamilyScorePersistenceEntity, Long> {}
