package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Creates the profile of an account that IAM has just verified.
 */
public record CreateProfileCommand(UserId userId, String name, SocialRole socialRole) {
}
