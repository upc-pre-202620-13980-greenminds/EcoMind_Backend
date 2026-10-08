package pe.greenminds.ecomind.community.domain.model.commands;
public record RegisterForEventCommand(Long eventId,Long userId,String registrationType,Long familyId){}
