package pe.greenminds.ecomind.users.application.internal.queryservices;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.users.application.queryservices.ProfileQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllUserProfilesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetUserProfileQuery;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@Service
@Transactional(readOnly = true)
public class ProfileQueryServiceImpl implements ProfileQueryService {

  private final UserProfileRepository userProfileRepository;

  public ProfileQueryServiceImpl(UserProfileRepository userProfileRepository) {
    this.userProfileRepository = userProfileRepository;
  }

  @Override
  public List<UserProfile> handle(GetAllUserProfilesQuery query) {
    return userProfileRepository.findAll();
  }

  @Override
  public Optional<UserProfile> handle(GetUserProfileQuery query) {
    return userProfileRepository.findById(query.userId());
  }
}
