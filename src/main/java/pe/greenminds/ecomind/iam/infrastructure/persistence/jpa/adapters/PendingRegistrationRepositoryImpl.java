package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.repositories.PendingRegistrationRepository;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.assemblers.PendingRegistrationPersistenceAssembler;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities.PendingRegistrationPersistenceEntity;
import pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.repositories.PendingRegistrationPersistenceRepository;

@Repository
public class PendingRegistrationRepositoryImpl implements PendingRegistrationRepository {

  private final PendingRegistrationPersistenceRepository persistenceRepository;
  private final EntityManager entities;

  public PendingRegistrationRepositoryImpl(
      PendingRegistrationPersistenceRepository persistenceRepository, EntityManager entities) {
    this.persistenceRepository = persistenceRepository;
    this.entities = entities;
  }

  @Override
  public PendingRegistration save(PendingRegistration pendingRegistration) {
    PendingRegistrationPersistenceEntity entity;
    if (pendingRegistration.getId() == null) {
      entity = PendingRegistrationPersistenceAssembler.toPersistenceFromDomain(pendingRegistration);
    } else {
      entity = persistenceRepository.findById(pendingRegistration.getId()).orElseThrow();
      PendingRegistrationPersistenceAssembler.copyMutableState(pendingRegistration, entity);
    }
    return PendingRegistrationPersistenceAssembler.toDomainFromPersistence(
        persistenceRepository.save(entity));
  }

  @Override
  public Optional<PendingRegistration> findByEmail(EmailAddress email) {
    return persistenceRepository
        .findByEmail(email.value())
        .map(PendingRegistrationPersistenceAssembler::toDomainFromPersistence);
  }

  @Override
  public void deleteByEmail(EmailAddress email) {
    lockEmail(email);
    persistenceRepository
        .findByEmail(email.value())
        .ifPresent(
            entity -> {
              persistenceRepository.delete(entity);
              // The email is unique: the delete must reach the database before the new insert.
              persistenceRepository.flush();
            });
  }

  private void lockEmail(EmailAddress email) {
    entities.unwrap(Session.class).doWork(connection -> {
      if ("PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
        // A row lock cannot protect the first attempt, before this email has a row.
        try (var timeout = connection.createStatement();
            var lock = connection.prepareStatement(
                "select pg_advisory_xact_lock(1938236103, hashtext(?))")) {
          timeout.execute("set local lock_timeout = '30s'");
          lock.setString(1, email.value());
          lock.execute();
        }
      }
    });
  }
}
