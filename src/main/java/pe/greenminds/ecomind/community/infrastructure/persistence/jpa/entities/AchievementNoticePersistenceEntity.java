package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "community_achievement_notices")
@Getter
@Setter
@NoArgsConstructor
public class AchievementNoticePersistenceEntity {
    @Id
    @Column(length = 36)
    private String awardId;

    @Column(nullable = false, length = 36)
    private String achievementId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant occurredAt;
}
