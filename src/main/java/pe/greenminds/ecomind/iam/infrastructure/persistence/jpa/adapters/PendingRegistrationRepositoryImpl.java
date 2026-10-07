package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.adapters;

import java.util.Optional;
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

  public PendingRegistrationRepositoryImpl(
      PendingRegistrationPersistenceRepository persistenceRepository) {
    this.persistenceRepository = persistenceRepository;
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
    persistenceRepository
        .findByEmail(email.value())
        .ifPresent(
            entity -> {
              persistenceRepository.delete(entity);
              // The email is unique: the delete must reach the database before the new insert.
              persistenceRepository.flush();
            });
  }
}
