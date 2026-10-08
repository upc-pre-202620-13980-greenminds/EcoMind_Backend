package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Stores a friend request and its state. The database also rejects requests to oneself and a
 * second request in the same direction between two users.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "friendships",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_friendships_requester_receiver",
            columnNames = {"requester_id", "receiver_id"}))
@Check(name = "ck_friendships_not_self", constraints = "requester_id <> receiver_id")
public class FriendshipPersistenceEntity extends AuditableAbstractPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long requesterId;

  @Column(nullable = false)
  private Long receiverId;

  @Column(nullable = false, length = 20)
  private String status;
}
