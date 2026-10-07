package pe.greenminds.ecomind.users.application.internal.commandservices;

import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.users.application.commandservices.FamilyCommandService;
import pe.greenminds.ecomind.users.application.internal.UsersErrors;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.commands.AddFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.commands.CreateFamilyCommand;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.events.FamilyMemberAddedEvent;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.domain.services.FamilyMembershipPolicy;

@Service
public class FamilyCommandServiceImpl implements FamilyCommandService {

  private final FamilyRepository familyRepository;
  private final UserProfileRepository userProfileRepository;
  private final FamilyMembershipPolicy familyMembershipPolicy;
  private final ApplicationEventPublisher eventPublisher;

  public FamilyCommandServiceImpl(
      FamilyRepository familyRepository,
      UserProfileRepository userProfileRepository,
      FamilyMembershipPolicy familyMembershipPolicy,
      ApplicationEventPublisher eventPublisher) {
    this.familyRepository = familyRepository;
    this.userProfileRepository = userProfileRepository;
    this.familyMembershipPolicy = familyMembershipPolicy;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Result<Family, ApplicationError> handle(CreateFamilyCommand command) {
    var parent = userProfileRepository.findById(command.parentUserId());
    if (parent.isEmpty()) {
      return Result.failure(UsersErrors.userProfileNotFound(command.parentUserId().value()));
    }
    if (!parent.get().isParent()) {
      return Result.failure(UsersErrors.familyCreatorNotParent());
    }
    if (!familyMembershipPolicy.canJoinAFamily(command.parentUserId())) {
      return Result.failure(UsersErrors.familyMembershipConflict());
    }
    Family family =
        familyRepository.save(
            Family.create(command.parentUserId(), command.name(), command.commitment()));
    return Result.success(family);
  }

  @Override
  @Transactional
  public Result<Family, ApplicationError> handle(AddFamilyMemberCommand command) {
    var family = familyRepository.findById(command.familyId());
    if (family.isEmpty()) {
      return Result.failure(UsersErrors.familyNotFound(command.familyId().value()));
    }
    if (!family.get().isManagedBy(command.requestedBy())) {
      return Result.failure(UsersErrors.familyAccessForbidden());
    }
    if (!userProfileRepository.existsById(command.memberUserId())) {
      return Result.failure(UsersErrors.userProfileNotFound(command.memberUserId().value()));
    }
    if (!familyMembershipPolicy.canJoinAFamily(command.memberUserId())) {
      return Result.failure(UsersErrors.familyMembershipConflict());
    }

    family.get().addMember(command.memberUserId(), command.familyRole());
    Family saved = familyRepository.save(family.get());
    eventPublisher.publishEvent(
        new FamilyMemberAddedEvent(saved.getId(), command.memberUserId(), Instant.now()));
    return Result.success(saved);
  }

  @Override
  @Transactional
  public Result<Family, ApplicationError> handle(RemoveFamilyMemberCommand command) {
    var family = familyRepository.findByMemberId(command.familyMemberId());
    if (family.isEmpty()) {
      return Result.failure(UsersErrors.familyMemberNotFound(command.familyMemberId()));
    }
    if (!family.get().isManagedBy(command.requestedBy())) {
      return Result.failure(UsersErrors.familyAccessForbidden());
    }
    boolean removesThemselves =
        family
            .get()
            .findMember(command.familyMemberId())
            .map(member -> member.getUserId().equals(command.requestedBy()))
            .orElse(false);
    if (removesThemselves) {
      return Result.failure(UsersErrors.familyParentSelfRemoval());
    }

    family.get().removeMember(command.familyMemberId());
    return Result.success(familyRepository.save(family.get()));
  }
}
