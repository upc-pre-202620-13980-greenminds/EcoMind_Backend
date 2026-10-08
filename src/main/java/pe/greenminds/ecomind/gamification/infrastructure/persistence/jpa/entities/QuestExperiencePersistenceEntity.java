package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gamification_quest_experiences")
@Getter
@Setter
@NoArgsConstructor
public class QuestExperiencePersistenceEntity {
    @Id private Long questId;

    @Column(nullable = false)
    private long experience;

    @Version private long version;
}
