package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.FamilyScore;

public record FamilyScoreResource(Long familyId, long totalEcopoints) {
  public static FamilyScoreResource from(FamilyScore score) {
    return new FamilyScoreResource(score.getFamilyId().value(), score.getTotalEcopoints());
  }
}
