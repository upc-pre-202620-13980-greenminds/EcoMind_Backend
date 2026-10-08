package pe.greenminds.ecomind.gamification.domain.model.queries;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public record GetFamilyAchievementsQuery(
        FamilyId familyId, UserId requestedBy, int page, int size) {}
