package pe.greenminds.ecomind.community.domain.model.commands;

public record LeaveEventCommand(Long eventId, Long requestedBy) {
}