package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_progresses")
public class UserProgressPersistenceEntity {
  /** IAM account id; no cross-context foreign key. */
  @Id private Long userId;

  @Column(nullable = false) private long totalEcopoints;
  @Column(nullable = false) private long totalExperience;
  @Column(nullable = false) private int currentStreak;
  @Column(nullable = false) private int longestStreak;
  private LocalDate lastActivityDate;

  @Version private long version;
}
