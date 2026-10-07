package pe.greenminds.ecomind.users.application.queryservices;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllUserProfilesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetUserProfileQuery;

public interface ProfileQueryService {

  List<UserProfile> handle(GetAllUserProfilesQuery query);

  Optional<UserProfile> handle(GetUserProfileQuery query);
}
