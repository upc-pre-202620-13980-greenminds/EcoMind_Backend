package pe.greenminds.ecomind.users.application.internal;

import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.ErrorType;

/**
 * Errors returned by the Users application services. Each code has its translated message in
 * messages.properties under the key error.&lt;code in lowercase with hyphens&gt;.
 */
public final class UsersErrors {

  private UsersErrors() {
  }

  public static ApplicationError userProfileNotFound(Long userId) {
    return ApplicationError.notFound("USER_PROFILE", String.valueOf(userId));
  }

  public static ApplicationError userProfileAlreadyExists() {
    return conflict("USER_PROFILE_CONFLICT", "The user already has a profile");
  }

  public static ApplicationError profileAccessForbidden() {
    return ApplicationError.forbidden(
        "PROFILE_ACCESS_FORBIDDEN", "Only the owner can update a profile");
  }

  public static ApplicationError familyNotFound(Long familyId) {
    return ApplicationError.notFound("FAMILY", String.valueOf(familyId));
  }

  public static ApplicationError familyMemberNotFound(Long familyMemberId) {
    return ApplicationError.notFound("FAMILY_MEMBER", String.valueOf(familyMemberId));
  }

  public static ApplicationError familyCreatorNotParent() {
    return ApplicationError.businessRuleViolation(
        "FAMILY_CREATOR_NOT_PARENT", "Only a parent can create a family");
  }

  public static ApplicationError familyMembershipConflict() {
    return conflict("FAMILY_MEMBERSHIP_CONFLICT", "The user already belongs to a family");
  }

  public static ApplicationError familyAccessForbidden() {
    return ApplicationError.forbidden(
        "FAMILY_ACCESS_FORBIDDEN", "Only a parent of the family can manage its members");
  }

  public static ApplicationError familyParentSelfRemoval() {
    return ApplicationError.businessRuleViolation(
        "FAMILY_PARENT_SELF_REMOVAL", "A parent cannot remove themselves from the family");
  }

  public static ApplicationError friendshipNotFound(Long friendshipId) {
    return ApplicationError.notFound("FRIENDSHIP", String.valueOf(friendshipId));
  }

  public static ApplicationError friendRequestToSelf() {
    return ApplicationError.businessRuleViolation(
        "FRIEND_REQUEST_TO_SELF", "A user cannot send a friend request to themselves");
  }

  public static ApplicationError friendshipConflict() {
    return conflict("FRIENDSHIP_CONFLICT", "There is already a friend request between the users");
  }

  public static ApplicationError friendRequestRejectedRecently() {
    return ApplicationError.businessRuleViolation(
        "FRIEND_REQUEST_REJECTED_RECENTLY",
        "A rejected friend request can be sent again after 7 days");
  }

  public static ApplicationError friendRequestAlreadyAnswered() {
    return ApplicationError.businessRuleViolation(
        "FRIEND_REQUEST_ALREADY_ANSWERED", "The friend request was already answered");
  }

  public static ApplicationError friendRequestAccessForbidden() {
    return ApplicationError.forbidden(
        "FRIEND_REQUEST_ACCESS_FORBIDDEN", "Only the receiver can answer a friend request");
  }

  private static ApplicationError conflict(String code, String message) {
    return new ApplicationError(ErrorType.CONFLICT, code, message, null);
  }
}
