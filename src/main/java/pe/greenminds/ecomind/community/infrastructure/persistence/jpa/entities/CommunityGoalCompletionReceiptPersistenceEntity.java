package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

@Entity
@Table(name = "community_goal_completion_receipts")
public class CommunityGoalCompletionReceiptPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @Column(name = "receipt_key", length = 180)
    private String receiptKey;

    protected CommunityGoalCompletionReceiptPersistenceEntity() {
    }
}