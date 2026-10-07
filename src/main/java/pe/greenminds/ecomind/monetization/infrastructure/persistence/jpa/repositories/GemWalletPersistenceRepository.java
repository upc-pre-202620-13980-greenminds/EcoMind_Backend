package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.GemWalletPersistenceEntity;

public interface GemWalletPersistenceRepository
    extends JpaRepository<GemWalletPersistenceEntity, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select wallet from GemWalletPersistenceEntity wallet where wallet.userId = :userId")
  Optional<GemWalletPersistenceEntity> lockByUserId(@Param("userId") Long userId);
}
