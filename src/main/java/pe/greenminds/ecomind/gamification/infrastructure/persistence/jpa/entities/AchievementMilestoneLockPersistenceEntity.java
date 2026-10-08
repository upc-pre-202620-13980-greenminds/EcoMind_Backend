package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "achievement_milestone_locks")
@Getter
@Setter
@NoArgsConstructor
public class AchievementMilestoneLockPersistenceEntity {
    @Id
    @Column(length = 48)
    private String beneficiary;
}
