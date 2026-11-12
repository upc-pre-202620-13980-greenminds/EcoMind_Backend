package pe.greenminds.ecomind.community.application.queryservices;

import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventChatAccessService {
    Result<Event, ApplicationError> requireParticipant(Long eventId, Long userId);
}