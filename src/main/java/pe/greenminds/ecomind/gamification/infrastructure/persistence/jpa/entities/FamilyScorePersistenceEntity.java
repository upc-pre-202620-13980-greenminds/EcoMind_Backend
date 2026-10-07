package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "family_scores")
@Getter
@Setter
@NoArgsConstructor
public class FamilyScorePersistenceEntity {
  @Id private Long familyId;
  @Column(nullable = false) private long totalEcopoints;
  @Version private long version;
}
