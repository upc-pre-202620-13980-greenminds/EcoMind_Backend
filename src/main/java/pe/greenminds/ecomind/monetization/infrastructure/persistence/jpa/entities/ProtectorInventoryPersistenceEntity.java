package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="protector_inventory", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","protector_id"}))
@Getter @Setter @NoArgsConstructor public class ProtectorInventoryPersistenceEntity {
 @Id @Column(length=36) private String id; @Column(name="user_id",nullable=false) private Long userId;
 @Column(name="protector_id",nullable=false,length=36) private String protectorId; @Column(nullable=false) private int quantity; @Version private long version;
}
