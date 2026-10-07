package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the normalized email and the password hash of an account. The plain password is never
 * stored.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "account_credentials",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_credentials_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_account_credentials_account_id", columnNames = "account_id")
    })
public class AccountCredentialPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountPersistenceEntity account;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String passwordHash;
}
