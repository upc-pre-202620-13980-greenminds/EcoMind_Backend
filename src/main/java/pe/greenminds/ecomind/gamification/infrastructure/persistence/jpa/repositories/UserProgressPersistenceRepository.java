package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities.UserProgressPersistenceEntity;

public interface UserProgressPersistenceRepository
        extends JpaRepository<UserProgressPersistenceEntity, Long> {}
