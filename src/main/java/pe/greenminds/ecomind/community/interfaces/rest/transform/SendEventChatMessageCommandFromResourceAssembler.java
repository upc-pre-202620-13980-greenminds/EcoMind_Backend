package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.commands.SendEventChatMessageCommand;
import pe.greenminds.ecomind.community.interfaces.rest.resources.SendEventChatMessageResource;

public final class SendEventChatMessageCommandFromResourceAssembler {
    private SendEventChatMessageCommandFromResourceAssembler() {
    }

    public static SendEventChatMessageCommand toCommandFromResource(
            SendEventChatMessageResource resource,
            Long eventId,
            Long requestedBy) {
        return new SendEventChatMessageCommand(
                eventId, requestedBy, resource.text(), resource.client_id());
    }
}