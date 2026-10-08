package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.commandservices.GemWalletCommandService;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;
import pe.greenminds.ecomind.monetization.domain.repositories.GemMovementRepository;
import pe.greenminds.ecomind.monetization.application.outboundservices.UserGemBalanceGateway;

@Service
public class GemWalletCommandServiceImpl implements GemWalletCommandService {
  private final UserGemBalanceGateway balances;
  private final GemMovementRepository movements;
  private final Clock clock;

  @Autowired
  public GemWalletCommandServiceImpl(UserGemBalanceGateway balances, GemMovementRepository movements) {
    this(balances, movements, Clock.systemUTC());
  }

  GemWalletCommandServiceImpl(UserGemBalanceGateway balances, GemMovementRepository movements, Clock clock) {
    this.balances = balances;
    this.movements = movements;
    this.clock = clock;
  }

  @Override
  @Transactional
  public GemWallet credit(Long userId, int amount, GemMovementType type,
      GemMovementOrigin origin, UUID referenceId) {
    requireRequest(type, origin, referenceId);
    if (!type.isCredit()) throw new IllegalArgumentException("A credit movement type is required");
    requireAmount(amount);
    var prior = movements.findByReferenceId(referenceId);
    if (prior.isPresent()) return replay(prior.get(), userId, amount, type, origin);
    var updated = new GemWallet(userId, balances.credit(userId, amount));
    movements.save(new GemMovement(UUID.randomUUID(), userId, type, origin,
        amount, updated.balance(), referenceId, Instant.now(clock)));
    return updated;
  }

  @Override
  @Transactional
  public GemWallet debit(Long userId, int amount, GemMovementOrigin origin, UUID referenceId) {
    requireRequest(GemMovementType.PURCHASE_DEBIT, origin, referenceId);
    requireAmount(amount);
    var prior = movements.findByReferenceId(referenceId);
    if (prior.isPresent()) return replay(prior.get(), userId, amount,
        GemMovementType.PURCHASE_DEBIT, origin);
    var updated = new GemWallet(userId, balances.debit(userId, amount));
    movements.save(new GemMovement(UUID.randomUUID(), userId, GemMovementType.PURCHASE_DEBIT, origin,
        amount, updated.balance(), referenceId, Instant.now(clock)));
    return updated;
  }

  private GemWallet getWallet(Long userId) {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    return new GemWallet(userId, balances.getBalance(userId));
  }

  private static void requireRequest(
      GemMovementType type, GemMovementOrigin origin, UUID referenceId) {
    if (type == null) throw new IllegalArgumentException("Movement type is required");
    if (origin == null) throw new IllegalArgumentException("Movement origin is required");
    if (referenceId == null) throw new IllegalArgumentException("Movement reference is required");
  }

  private static void requireAmount(int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Gem amount must be positive");
  }

  private GemWallet replay(GemMovement prior, Long userId, int amount,
      GemMovementType type, GemMovementOrigin origin) {
    if (!prior.userId().equals(userId) || prior.amount() != amount
        || prior.type() != type || prior.origin() != origin) {
      throw new IllegalArgumentException("Movement reference was already used for another operation");
    }
    return getWallet(userId);
  }
}
