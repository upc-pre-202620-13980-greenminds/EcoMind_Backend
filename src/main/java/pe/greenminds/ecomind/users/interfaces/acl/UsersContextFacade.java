package pe.greenminds.ecomind.users.interfaces.acl;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.application.queryservices.FamilyQueryService;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyQuery;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/** Public integration boundary; other contexts receive primitive identities only. */
@Service
public class UsersContextFacade {
  private final FamilyQueryService families;

  public UsersContextFacade(FamilyQueryService families) { this.families = families; }

  public boolean familyExists(Long familyId) {
    return families.handle(new GetFamilyQuery(new FamilyId(familyId))).isPresent();
  }

  public boolean isFamilyMember(Long familyId, Long userId) {
    return families.handle(new GetFamilyQuery(new FamilyId(familyId)))
        .map(family -> family.hasMember(new UserId(userId))).orElse(false);
  }
}
