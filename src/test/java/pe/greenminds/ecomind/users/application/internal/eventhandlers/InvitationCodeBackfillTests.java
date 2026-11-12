package pe.greenminds.ecomind.users.application.internal.eventhandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.InvitationCodeGenerator;

@ExtendWith(MockitoExtension.class)
class InvitationCodeBackfillTests {

  @Mock
  private UserProfileRepository userProfileRepository;

  private InvitationCodeBackfill backfill;

  @BeforeEach
  void setUp() {
    backfill =
        new InvitationCodeBackfill(
            userProfileRepository, new InvitationCodeGenerator(userProfileRepository));
  }

  @Test
  void profilesWithoutACodeGetOneWhenTheApplicationStarts() {
    UserProfile oldProfile = UserProfile.create(new UserId(3L), "Luis", SocialRole.STUDENT);
    when(userProfileRepository.findAllWithoutInvitationCode()).thenReturn(List.of(oldProfile));

    backfill.run(null);

    assertThat(oldProfile.getInvitationCode()).isNotNull();
    verify(userProfileRepository).save(oldProfile);
  }

  @Test
  void nothingIsSavedWhenEveryProfileAlreadyHasACode() {
    when(userProfileRepository.findAllWithoutInvitationCode()).thenReturn(List.of());

    backfill.run(null);

    verify(userProfileRepository, never()).save(any());
  }
}
