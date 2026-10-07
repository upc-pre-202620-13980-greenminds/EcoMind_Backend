package pe.greenminds.ecomind.users.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.commands.AddFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.commands.CreateFamilyCommand;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.entities.FamilyMember;
import pe.greenminds.ecomind.users.domain.model.events.FamilyMemberAddedEvent;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.FamilyMembershipPolicy;

@ExtendWith(MockitoExtension.class)
class FamilyCommandServiceImplTests {

  private static final UserId PARENT_ID = new UserId(1L);
  private static final UserId CHILD_ID = new UserId(2L);
  private static final FamilyId FAMILY_ID = new FamilyId(10L);
  private static final Long PARENT_MEMBER_ID = 100L;
  private static final Long CHILD_MEMBER_ID = 101L;

  @Mock
  private FamilyRepository familyRepository;
  @Mock
  private UserProfileRepository userProfileRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private FamilyCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new FamilyCommandServiceImpl(
            familyRepository,
            userProfileRepository,
            new FamilyMembershipPolicy(familyRepository),
            eventPublisher);
  }

  @Test
  void createFamilyMakesTheParentItsFirstMember() {
    when(userProfileRepository.findById(PARENT_ID))
        .thenReturn(Optional.of(profile(PARENT_ID, SocialRole.PARENT)));
    when(familyRepository.existsByMemberUserId(PARENT_ID)).thenReturn(false);
    when(familyRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result =
        service.handle(new CreateFamilyCommand(PARENT_ID, "Torres Family", "Save water"));

    Family family = result.toOptional().orElseThrow();
    assertThat(family.getName()).isEqualTo("Torres Family");
    assertThat(family.getMembers()).hasSize(1);
    assertThat(family.isManagedBy(PARENT_ID)).isTrue();
  }

  @Test
  void createFamilyFailsWhenTheCreatorIsNotAParent() {
    when(userProfileRepository.findById(CHILD_ID))
        .thenReturn(Optional.of(profile(CHILD_ID, SocialRole.STUDENT)));

    var result = service.handle(new CreateFamilyCommand(CHILD_ID, "Friends Club", "Recycle"));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_CREATOR_NOT_PARENT");
    verify(familyRepository, never()).save(any());
  }

  @Test
  void createFamilyFailsWhenTheParentAlreadyBelongsToAFamily() {
    when(userProfileRepository.findById(PARENT_ID))
        .thenReturn(Optional.of(profile(PARENT_ID, SocialRole.PARENT)));
    when(familyRepository.existsByMemberUserId(PARENT_ID)).thenReturn(true);

    var result =
        service.handle(new CreateFamilyCommand(PARENT_ID, "Torres Family", "Save water"));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_MEMBERSHIP_CONFLICT");
    verify(familyRepository, never()).save(any());
  }

  @Test
  void addFamilyMemberAddsTheUserAndPublishesTheEvent() {
    when(familyRepository.findById(FAMILY_ID)).thenReturn(Optional.of(familyWithParent()));
    when(userProfileRepository.existsById(CHILD_ID)).thenReturn(true);
    when(familyRepository.existsByMemberUserId(CHILD_ID)).thenReturn(false);
    when(familyRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result =
        service.handle(
            new AddFamilyMemberCommand(PARENT_ID, FAMILY_ID, CHILD_ID, FamilyRole.CHILD));

    Family family = result.toOptional().orElseThrow();
    assertThat(family.getMembers()).hasSize(2);
    assertThat(family.findMemberByUser(CHILD_ID).orElseThrow().getFamilyRole())
        .isEqualTo(FamilyRole.CHILD);
    verify(eventPublisher).publishEvent(any(FamilyMemberAddedEvent.class));
  }

  @Test
  void addFamilyMemberIsForbiddenForSomeoneWhoIsNotAParentOfTheFamily() {
    when(familyRepository.findById(FAMILY_ID)).thenReturn(Optional.of(familyWithParentAndChild()));

    var result =
        service.handle(
            new AddFamilyMemberCommand(CHILD_ID, FAMILY_ID, new UserId(3L), FamilyRole.CHILD));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_ACCESS_FORBIDDEN");
    verify(familyRepository, never()).save(any());
  }

  @Test
  void addFamilyMemberFailsWhenTheUserAlreadyBelongsToAFamily() {
    when(familyRepository.findById(FAMILY_ID)).thenReturn(Optional.of(familyWithParent()));
    when(userProfileRepository.existsById(CHILD_ID)).thenReturn(true);
    when(familyRepository.existsByMemberUserId(CHILD_ID)).thenReturn(true);

    var result =
        service.handle(
            new AddFamilyMemberCommand(PARENT_ID, FAMILY_ID, CHILD_ID, FamilyRole.CHILD));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_MEMBERSHIP_CONFLICT");
    verify(familyRepository, never()).save(any());
  }

  @Test
  void addFamilyMemberFailsWhenTheUserHasNoProfile() {
    when(familyRepository.findById(FAMILY_ID)).thenReturn(Optional.of(familyWithParent()));
    when(userProfileRepository.existsById(CHILD_ID)).thenReturn(false);

    var result =
        service.handle(
            new AddFamilyMemberCommand(PARENT_ID, FAMILY_ID, CHILD_ID, FamilyRole.CHILD));

    assertThat(errorCodeOf(result)).isEqualTo("USER_PROFILE_NOT_FOUND");
  }

  @Test
  void removeFamilyMemberRemovesTheMember() {
    when(familyRepository.findByMemberId(CHILD_MEMBER_ID))
        .thenReturn(Optional.of(familyWithParentAndChild()));
    when(familyRepository.save(any())).thenAnswer(call -> call.getArgument(0));

    var result = service.handle(new RemoveFamilyMemberCommand(PARENT_ID, CHILD_MEMBER_ID));

    Family family = result.toOptional().orElseThrow();
    assertThat(family.hasMember(CHILD_ID)).isFalse();
    assertThat(family.getMembers()).hasSize(1);
  }

  @Test
  void removeFamilyMemberFailsWhenAParentRemovesThemselves() {
    when(familyRepository.findByMemberId(PARENT_MEMBER_ID))
        .thenReturn(Optional.of(familyWithParentAndChild()));

    var result = service.handle(new RemoveFamilyMemberCommand(PARENT_ID, PARENT_MEMBER_ID));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_PARENT_SELF_REMOVAL");
    verify(familyRepository, never()).save(any());
  }

  @Test
  void removeFamilyMemberFailsWhenTheMembershipDoesNotExist() {
    when(familyRepository.findByMemberId(999L)).thenReturn(Optional.empty());

    var result = service.handle(new RemoveFamilyMemberCommand(PARENT_ID, 999L));

    assertThat(errorCodeOf(result)).isEqualTo("FAMILY_MEMBER_NOT_FOUND");
  }

  private static UserProfile profile(UserId userId, SocialRole socialRole) {
    return UserProfile.create(userId, "Test User", socialRole);
  }

  private static Family familyWithParent() {
    return new Family(
        FAMILY_ID,
        "Torres Family",
        "Save water",
        List.of(new FamilyMember(PARENT_MEMBER_ID, PARENT_ID, FamilyRole.PARENT)));
  }

  private static Family familyWithParentAndChild() {
    return new Family(
        FAMILY_ID,
        "Torres Family",
        "Save water",
        List.of(
            new FamilyMember(PARENT_MEMBER_ID, PARENT_ID, FamilyRole.PARENT),
            new FamilyMember(CHILD_MEMBER_ID, CHILD_ID, FamilyRole.CHILD)));
  }

  private static String errorCodeOf(Result<?, ApplicationError> result) {
    assertThat(result.isFailure()).isTrue();
    return ((Result.Failure<?, ApplicationError>) result).error().code();
  }
}
