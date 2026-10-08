package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentStatus;

@Entity
@Table(
    name = "gem_purchases",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_gem_purchase_request", columnNames = "request_id"),
      @UniqueConstraint(name = "uq_gem_purchase_payment_reference", columnNames = "payment_reference")
    },
    indexes = @Index(name = "ix_gem_purchase_user_date", columnList = "user_id,created_at"))
@Getter
@Setter
@NoArgsConstructor
public class GemPurchasePersistenceEntity {
  @Id @Column(length = 36) private String id;
  @Column(name = "request_id", nullable = false, length = 36) private String requestId;
  @Column(name = "user_id", nullable = false) private Long userId;
  @Column(name = "package_id", nullable = false, length = 36) private String packageId;
  @Column(name = "gem_amount", nullable = false) private int gemAmount;
  @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2) private BigDecimal amountPaid;
  @Column(nullable = false, length = 3) private String currency;
  @Enumerated(EnumType.STRING) @Column(name = "payment_method", nullable = false, length = 16)
  private PaymentMethodType paymentMethod;
  @Enumerated(EnumType.STRING) @Column(name = "payment_status", nullable = false, length = 16)
  private PaymentStatus paymentStatus;
  @Column(name = "payment_reference", length = 120) private String paymentReference;
  @Column(name = "created_at", nullable = false) private Instant createdAt;
  @Column(name = "completed_at") private Instant completedAt;
  @Version private long version;
}
