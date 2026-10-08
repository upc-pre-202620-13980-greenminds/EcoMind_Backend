package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.users.domain.model.valueobjects.FamilyRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public record AddFamilyMemberCommand(
    UserId requestedBy, FamilyId familyId, UserId memberUserId, FamilyRole familyRole) {
}
