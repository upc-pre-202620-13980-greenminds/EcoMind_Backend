package pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tutorial_progress", uniqueConstraints =
    @UniqueConstraint(name = "uk_tutorial_progress_user", columnNames = "user_id"))
@Getter @Setter @NoArgsConstructor
public class TutorialProgressEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "user_id", nullable = false) private Long userId;
  @Column(name = "current_step", nullable = false) private int currentStep = 1;
  @Column(name = "total_steps", nullable = false) private int totalSteps = 7;
  @Column(nullable = false) private boolean completed;
  @Column(name = "completed_at") private Instant completedAt;
}
