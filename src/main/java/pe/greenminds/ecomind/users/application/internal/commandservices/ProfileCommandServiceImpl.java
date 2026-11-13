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
import pe.greenminds.ecomind.users.domain.services.InvitationCodeGenerator;

@Service
public class ProfileCommandServiceImpl implements ProfileCommandService {

  private final UserProfileRepository userProfileRepository;
  private final InvitationCodeGenerator invitationCodeGenerator;
  private final ApplicationEventPublisher eventPublisher;

  public ProfileCommandServiceImpl(
      UserProfileRepository userProfileRepository,
      InvitationCodeGenerator invitationCodeGenerator,
      ApplicationEventPublisher eventPublisher) {
    this.userProfileRepository = userProfileRepository;
    this.invitationCodeGenerator = invitationCodeGenerator;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Result<UserProfile, ApplicationError> handle(CreateProfileCommand command) {
    if (userProfileRepository.existsById(command.userId())) {
      return Result.failure(UsersErrors.userProfileAlreadyExists());
    }
    UserProfile newProfile =
        UserProfile.create(command.userId(), command.name(), command.socialRole());
    newProfile.assignInvitationCode(invitationCodeGenerator.newUniqueCode());
    UserProfile profile = userProfileRepository.save(newProfile);
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
    // Both changes happen in memory; the profile is saved once at the end
    profile.get().rename(command.name());
    profile
        .get()
        .updateProgress(
            command.streak(), command.lastStreakDate(), command.ecopoints(), command.gemBalance());
    return Result.success(userProfileRepository.save(profile.get()));
  }
}
