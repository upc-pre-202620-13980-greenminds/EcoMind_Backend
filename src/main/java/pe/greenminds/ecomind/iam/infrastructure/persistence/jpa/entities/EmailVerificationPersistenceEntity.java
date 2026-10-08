package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the hash of the verification code of a pending registration, never the code itself.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "email_verifications")
public class EmailVerificationPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "pending_registration_id", nullable = false)
  private PendingRegistrationPersistenceEntity pendingRegistration;

  @Column(nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant usedAt;

  @Column(nullable = false)
  private int attempts;
}
