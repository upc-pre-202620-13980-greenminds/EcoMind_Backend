package pe.greenminds.ecomind.users.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.commands.CreateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.commands.UpdateProfileCommand;
import pe.greenminds.ecomind.users.domain.model.events.ProfileCreatedEvent;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
class ProfileCommandServiceImplTests {

  private static final UserId USER_ID = new UserId(7L);
  private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);

  @Mock
  private UserProfileRepository userProfileRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private ProfileCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ProfileCommandServiceImpl(userProfileRepository, eventPublisher);
  }

  @Test
  void createProfileStartsWithoutProgressAndPublishesTheEvent() {
    when(userProfileRepository.existsById(USER_ID)).thenReturn(false);
    when(userProfileRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result =
        service.handle(new CreateProfileCommand(USER_ID, " Camila Torres ", SocialRole.STUDENT));

    UserProfile profile = result.toOptional().orElseThrow();
    assertThat(profile.getUserId()).isEqualTo(USER_ID);
    assertThat(profile.getName()).isEqualTo("Camila Torres");
    assertThat(profile.getSocialRole()).isEqualTo(SocialRole.STUDENT);
    assertThat(profile.getStreak()).isZero();
    assertThat(profile.getEcopoints()).isZero();
    assertThat(profile.getGemBalance()).isZero();
    verify(eventPublisher).publishEvent(any(ProfileCreatedEvent.class));
  }

  @Test
  void createProfileFailsWhenTheUserAlreadyHasOne() {
    when(userProfileRepository.existsById(USER_ID)).thenReturn(true);

    var result =
        service.handle(new CreateProfileCommand(USER_ID, "Camila Torres", SocialRole.STUDENT));

    assertThat(errorCodeOf(result)).isEqualTo("USER_PROFILE_CONFLICT");
    verify(userProfileRepository, never()).save(any());
  }

  @Test
  void updateProfileReplacesTheProgressOfTheOwner() {
    UserProfile profile = UserProfile.create(USER_ID, "Camila Torres", SocialRole.STUDENT);
    when(userProfileRepository.findById(USER_ID)).thenReturn(Optional.of(profile));
    when(userProfileRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new UpdateProfileCommand(USER_ID, USER_ID, 3, TODAY, 120, 10));

    UserProfile updated = result.toOptional().orElseThrow();
    assertThat(updated.getStreak()).isEqualTo(3);
    assertThat(updated.getLastStreakDate()).isEqualTo(TODAY);
    assertThat(updated.getEcopoints()).isEqualTo(120);
    assertThat(updated.getGemBalance()).isEqualTo(10);
  }

  @Test
  void updateProfileIsForbiddenForAnotherUser() {
    var result =
        service.handle(new UpdateProfileCommand(new UserId(99L), USER_ID, 3, TODAY, 120, 10));

    assertThat(errorCodeOf(result)).isEqualTo("PROFILE_ACCESS_FORBIDDEN");
    verify(userProfileRepository, never()).save(any());
  }

  @Test
  void updateProfileFailsWhenTheProfileDoesNotExist() {
    when(userProfileRepository.findById(USER_ID)).thenReturn(Optional.empty());

    var result = service.handle(new UpdateProfileCommand(USER_ID, USER_ID, 3, TODAY, 120, 10));

    assertThat(errorCodeOf(result)).isEqualTo("USER_PROFILE_NOT_FOUND");
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
