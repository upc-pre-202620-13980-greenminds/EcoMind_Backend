package pe.greenminds.ecomind.community.application.internal.commandservices;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.EventChatCommandService;
import pe.greenminds.ecomind.community.application.queryservices.EventChatAccessService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.domain.model.aggregates.EventChatMessage;
import pe.greenminds.ecomind.community.domain.model.commands.SendEventChatMessageCommand;
import pe.greenminds.ecomind.community.domain.repositories.EventChatMessageRepository;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class EventChatCommandServiceImpl implements EventChatCommandService {
    private final EventChatAccessService access;
    private final EventChatMessageRepository messages;
    private final CommunityActorGateway actors;

    public EventChatCommandServiceImpl(
            EventChatAccessService access,
            EventChatMessageRepository messages,
            CommunityActorGateway actors) {
        this.access = access;
        this.messages = messages;
        this.actors = actors;
    }

    @Override
    @Transactional
    public Result<EventChatMessage, ApplicationError> handle(
            SendEventChatMessageCommand command) {
        if (command.text() == null
                || command.text().strip().isEmpty()
                || command.text().strip().length() > 2000
                || command.clientId() == null) {
            return Result.failure(ApplicationError.validationError(
                    "Event chat message",
                    "Text and client id are required; text must not exceed 2000 characters"));
        }

        return access.requireParticipant(command.eventId(), command.requestedBy())
                .flatMap(event -> {
                    var previous = messages.findByClientId(
                            command.eventId(),
                            command.requestedBy(),
                            command.clientId());

                    if (previous.isPresent()) {
                        if (!previous.get().text().equals(command.text().strip())) {
                            return Result.failure(ApplicationError.conflict(
                                    "Event chat message",
                                    "Client id was already used for a different message"));
                        }

                        return Result.success(previous.get());
                    }

                    var message = new EventChatMessage(
                            null,
                            event.getId(),
                            command.requestedBy(),
                            actors.getUserName(command.requestedBy()),
                            command.text(),
                            Instant.now(),
                            command.clientId());

                    return Result.success(messages.save(message));
                });
    }
}