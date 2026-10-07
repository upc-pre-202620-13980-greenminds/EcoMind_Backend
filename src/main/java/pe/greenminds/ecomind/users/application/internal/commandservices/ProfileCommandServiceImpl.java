package pe.greenminds.ecomind.users.application.internal.commandservices;

import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.application.commandservices.ProfileCommandService;
import pe.greenminds.ecomind.users.application.internal.UsersErrors;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.commands.CreateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.commands.UpdateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.events.ProfileCreatedEvent;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

  private final UserProfileRepository userProfileRepository;
  private final ApplicationEventPublisher eventPublisher;

  public ProfileCommandServiceImpl(
      UserProfileRepository userProfileRepository, ApplicationEventPublisher eventPublisher) {
    this.userProfileRepository = userProfileRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Result<UserProfile, ApplicationError> handle(CreateProfileCommand command) {
    if (userProfileRepository.existsById(command.userId())) {
      return Result.failure(UsersErrors.userProfileAlreadyExists());
    }
    UserProfile profile =
        userProfileRepository.save(
            UserProfile.create(command.userId(), command.name(), command.socialRole()));
    eventPublisher.publishEvent(new ProfileCreatedEvent(profile.getUserId(), Instant.now()));
    return Result.success(profile);
  }

  @Override
  @Transactional
  public Result<UserProfile, ApplicationError> handle(UpdateProfileCommand command) {
    if (!command.requestedBy().equals(command.userId())) {
      return Result.failure(UsersErrors.profileAccessForbidden());
    }
    var profile = userProfileRepository.findById(command.userId());
    if (profile.isEmpty()) {
      return Result.failure(UsersErrors.userProfileNotFound(command.userId().value()));
    }
    profile
        .get()
        .updateProgress(
            command.streak(), command.lastStreakDate(), command.ecopoints(), command.gemBalance());
    return Result.success(userProfileRepository.save(profile.get()));
  }
}
