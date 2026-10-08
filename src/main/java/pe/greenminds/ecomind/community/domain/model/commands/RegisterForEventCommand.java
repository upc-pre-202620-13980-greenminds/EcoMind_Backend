package pe.greenminds.ecomind.community.domain.model.commands;

import pe.greenminds.ecomind.community.domain.model.valueobjects.EventRegistrationType;

public record RegisterForEventCommand(Long eventId, Long userId, EventRegistrationType registrationType, Long familyId) {}
