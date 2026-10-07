package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gem_wallets")
@Getter
@Setter
@NoArgsConstructor
public class GemWalletPersistenceEntity {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @Column(nullable = false)
  private int balance;

  @Version
  private long version;
}
