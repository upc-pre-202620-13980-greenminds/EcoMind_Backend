package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.valueobjects.EventParticipation;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventParticipationResource;

public final class EventParticipationResourceFromEntityAssembler {
    private EventParticipationResourceFromEntityAssembler() {
    }

    public static EventParticipationResource toResourceFromEntity(
            EventParticipation participation) {
        return new EventParticipationResource(
                EventResourceFromEntityAssembler.toResourceFromEntity(
                        participation.event()),
                participation.communityName(),
                participation.creatorName(),
                participation.participantCount(),
                participation.totalCapacity(),
                participation.registrationId(),
                participation.joined(),
                participation.canCancelGroup(),
                participation.creator(),
                participation.chatExpiresAt(),
                participation.chatAvailable(),
                participation.familyId());
    }
}