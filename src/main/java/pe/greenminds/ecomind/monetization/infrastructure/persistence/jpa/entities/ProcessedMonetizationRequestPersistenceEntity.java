package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_monetization_requests")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedMonetizationRequestPersistenceEntity {
  @Id @Column(length = 36) private String requestId;
  @Column(nullable = false, length = 40) private String operation;
  @Column(nullable = false) private Instant processedAt;
}
