package pe.greenminds.ecomind.gamification.domain.model.aggregates;

import java.util.Objects;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;

/** Only rewards explicitly attributed to the family contribute to this score. */
public class FamilyScore {
  private final FamilyId familyId;
  private long totalEcopoints;

  public FamilyScore(FamilyId familyId, long totalEcopoints) {
    this.familyId = Objects.requireNonNull(familyId);
    if (totalEcopoints < 0) throw new IllegalArgumentException("Ecopoints cannot be negative");
    this.totalEcopoints = totalEcopoints;
  }

  public void addReward(long ecopoints) {
    if (ecopoints < 0) throw new IllegalArgumentException("Ecopoints cannot be negative");
    totalEcopoints = Math.addExact(totalEcopoints, ecopoints);
  }

  public FamilyId getFamilyId() { return familyId; }
  public long getTotalEcopoints() { return totalEcopoints; }
}
