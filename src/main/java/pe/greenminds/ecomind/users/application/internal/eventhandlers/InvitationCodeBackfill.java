package pe.greenminds.ecomind.users.application.internal.eventhandlers;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.InvitationCodeGenerator;

/**
 * Gives an invitation code to the profiles created before the code existed. It runs every time the
 * application starts and does nothing once every profile has one.
 */
@Component
public class InvitationCodeBackfill implements ApplicationRunner {

  private final UserProfileRepository userProfileRepository;
  private final InvitationCodeGenerator invitationCodeGenerator;

  public InvitationCodeBackfill(
      UserProfileRepository userProfileRepository,
      InvitationCodeGenerator invitationCodeGenerator) {
    this.userProfileRepository = userProfileRepository;
    this.invitationCodeGenerator = invitationCodeGenerator;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    for (UserProfile profile : userProfileRepository.findAllWithoutInvitationCode()) {
      profile.assignInvitationCode(invitationCodeGenerator.newUniqueCode());
      userProfileRepository.save(profile);
    }
  }
}
