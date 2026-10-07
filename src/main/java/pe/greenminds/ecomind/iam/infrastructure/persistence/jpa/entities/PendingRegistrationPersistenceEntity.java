package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a registration until its email is verified, with the password already hashed.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "pending_registrations",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_pending_registrations_email", columnNames = "email"))
public class PendingRegistrationPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, length = 20)
  private String socialRole;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @OneToOne(
      mappedBy = "pendingRegistration",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      optional = false)
  private EmailVerificationPersistenceEntity emailVerification;
}
