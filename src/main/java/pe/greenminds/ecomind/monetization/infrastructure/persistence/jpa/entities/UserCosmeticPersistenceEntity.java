package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="user_cosmetics", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","cosmetic_id"}))
@Getter @Setter @NoArgsConstructor public class UserCosmeticPersistenceEntity {
 @Id @Column(length=36) private String id; @Column(name="user_id",nullable=false) private Long userId;
 @Column(name="cosmetic_id",nullable=false,length=36) private String cosmeticId; @Column(nullable=false) private boolean equipped;
}
