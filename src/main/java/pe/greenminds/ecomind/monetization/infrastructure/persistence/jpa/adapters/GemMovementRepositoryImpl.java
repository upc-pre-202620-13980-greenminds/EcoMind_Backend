package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;
import pe.greenminds.ecomind.monetization.domain.repositories.GemMovementRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemMovementPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.GemMovementPersistenceRepository;

@Repository
public class GemMovementRepositoryImpl implements GemMovementRepository {
  private final GemMovementPersistenceRepository persistence;

  public GemMovementRepositoryImpl(GemMovementPersistenceRepository persistence) {
    this.persistence = persistence;
  }

  @Override
  public Optional<GemMovement> findByReferenceId(UUID referenceId) {
    return persistence.findByReferenceId(referenceId.toString()).map(GemMovementRepositoryImpl::toDomain);
  }

  @Override
  public List<GemMovement> findRecentByUserId(Long userId) {
    return persistence.findTop100ByUserIdOrderByOccurredAtDesc(userId).stream()
        .map(GemMovementRepositoryImpl::toDomain).toList();
  }

  @Override
  public GemMovement save(GemMovement movement) {
    var entity = new GemMovementPersistenceEntity();
    entity.setId((movement.id() == null ? UUID.randomUUID() : movement.id()).toString());
    entity.setUserId(movement.userId());
    entity.setType(movement.type().name());
    entity.setOrigin(movement.origin().name());
    entity.setAmount(movement.amount());
    entity.setBalanceAfter(movement.balanceAfter());
    entity.setReferenceId(movement.referenceId().toString());
    entity.setOccurredAt(movement.occurredAt());
    return toDomain(persistence.save(entity));
  }

  private static GemMovement toDomain(GemMovementPersistenceEntity entity) {
    return new GemMovement(UUID.fromString(entity.getId()), entity.getUserId(),
        GemMovementType.valueOf(entity.getType()), GemMovementOrigin.valueOf(entity.getOrigin()),
        entity.getAmount(), entity.getBalanceAfter(), UUID.fromString(entity.getReferenceId()),
        entity.getOccurredAt());
  }
}
