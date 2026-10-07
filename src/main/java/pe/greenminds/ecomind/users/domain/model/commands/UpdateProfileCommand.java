package pe.greenminds.ecomind.users.domain.model.commands;

import java.time.LocalDate;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

/**
 * Updates the progress shown in a profile. Only the owner of the profile can do it.
 */
public record UpdateProfileCommand(
    UserId requestedBy,
    UserId userId,
    int streak,
    LocalDate lastStreakDate,
    int ecopoints,
    int gemBalance) {
}
