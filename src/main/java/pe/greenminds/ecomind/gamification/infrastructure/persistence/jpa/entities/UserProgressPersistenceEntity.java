package pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Check(
        name = "ck_userprogress",
        constraints =
                "total_ecopoints >= 0 AND total_experience >= 0 AND current_streak >= 0 AND"
                    + " longest_streak >= current_streak")
@Table(name = "user_progresses")
public class UserProgressPersistenceEntity {
    /** IAM account id; no cross-context foreign key. */
    @Id private Long userId;

    @Column(nullable = false)
    private long totalEcopoints;

    /** Deprecated schema column mirrored on writes; never a separate score. */
    @Column(name = "total_experience", nullable = false)
    private long legacyTotalExperience;

    @Column(nullable = false)
    private int currentStreak;

    @Column(nullable = false)
    private int longestStreak;

    private LocalDate lastActivityDate;
    private LocalDate lastProtectedDate;

    @Version private long version;
}
