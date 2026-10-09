package pe.greenminds.ecomind.community.domain.model.commands;

public record CancelEventRegistrationCommand(Long eventId,Long registrationId,Long requestedBy){}
