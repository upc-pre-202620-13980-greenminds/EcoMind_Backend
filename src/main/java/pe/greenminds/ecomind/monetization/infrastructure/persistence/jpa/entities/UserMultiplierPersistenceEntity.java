package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import lombok.*;
@Entity @Table(name="user_multipliers",indexes=@Index(columnList="user_id,starts_at,expires_at"))
@Getter @Setter @NoArgsConstructor public class UserMultiplierPersistenceEntity {
 @Id @Column(length=36) private String id; @Column(name="user_id",nullable=false) private Long userId;
 @Column(name="multiplier_id",nullable=false,length=36) private String multiplierId; @Column(nullable=false,precision=5,scale=2) private BigDecimal factor;
 @Column(name="starts_at",nullable=false) private Instant startsAt; @Column(name="expires_at",nullable=false) private Instant expiresAt;
}
