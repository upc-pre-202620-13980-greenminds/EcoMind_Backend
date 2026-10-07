package pe.greenminds.ecomind.users.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a member of a family. The unique user id enforces that a user belongs to one family.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "family_members",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_family_members_user_id", columnNames = "user_id"))
public class FamilyMemberPersistenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "family_id", nullable = false)
  private FamilyPersistenceEntity family;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, length = 30)
  private String familyRole;
}
