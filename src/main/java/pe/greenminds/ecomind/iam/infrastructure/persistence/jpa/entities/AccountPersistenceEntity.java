package pe.greenminds.ecomind.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String status;

  @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, optional = false)
  private AccountCredentialPersistenceEntity credential;
}
