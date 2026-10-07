package pe.greenminds.ecomind.gamification.application.outboundservices;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;

public interface UsersServiceClient {
  boolean familyExists(FamilyId familyId);
  boolean isFamilyMember(FamilyId familyId, UserId userId);
}
