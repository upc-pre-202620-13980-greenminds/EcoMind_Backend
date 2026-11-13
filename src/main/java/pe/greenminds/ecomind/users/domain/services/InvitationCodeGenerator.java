package pe.greenminds.ecomind.users.domain.services;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.users.domain.model.valueobjects.InvitationCode;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

/**
 * Gives a new invitation code that no other profile has. The profile cannot check this by itself
 * because it needs the codes stored in the database.
 */
@Service
public class InvitationCodeGenerator {

  private final UserProfileRepository userProfileRepository;

  public InvitationCodeGenerator(UserProfileRepository userProfileRepository) {
    this.userProfileRepository = userProfileRepository;
  }

  /** Draws codes until one is free; the unique constraint of the column is the last guard. */
  public InvitationCode newUniqueCode() {
    InvitationCode code = InvitationCode.generate();
    while (userProfileRepository.existsByInvitationCode(code)) {
      code = InvitationCode.generate();
    }
    return code;
  }
}
