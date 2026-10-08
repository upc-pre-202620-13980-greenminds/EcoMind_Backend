package pe.greenminds.ecomind.users.domain.model.commands;

import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;

public record CreateFamilyCommand(UserId parentUserId, String name, String commitment) {
}
