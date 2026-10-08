package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "gamification_minigame_completions",
        indexes =
                @Index(name = "ix_minigame_window", columnList = "user_id,minigame_id,occurred_at"))
@Getter
@Setter
@NoArgsConstructor
public class MinigameCompletionPersistenceEntity {
    @Id
    @Column(length = 36)
    private String executionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 36)
    private String minigameId;

    @Column(nullable = false)
    private Instant occurredAt;
}
