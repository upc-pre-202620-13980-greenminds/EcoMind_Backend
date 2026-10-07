package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the hash, expiration and use of a password recovery token, never the token itself.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "password_reset_tokens",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_password_reset_tokens_token_hash", columnNames = "token_hash"))
public class PasswordResetTokenPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountPersistenceEntity account;

  @Column(nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant usedAt;
}
