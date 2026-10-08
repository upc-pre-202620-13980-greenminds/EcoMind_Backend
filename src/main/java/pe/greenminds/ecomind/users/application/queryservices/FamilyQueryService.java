package pe.greenminds.ecomind.users.application.queryservices;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFamiliesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyMembersByUserQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyQuery;

public interface FamilyQueryService {

  List<Family> handle(GetAllFamiliesQuery query);

  Optional<Family> handle(GetFamilyQuery query);

  /** Family the user belongs to, if any. */
  Optional<Family> handle(GetFamilyMembersByUserQuery query);
}
