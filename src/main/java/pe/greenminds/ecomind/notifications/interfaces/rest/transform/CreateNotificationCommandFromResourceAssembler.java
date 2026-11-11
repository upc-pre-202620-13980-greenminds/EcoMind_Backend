package pe.greenminds.ecomind.notifications.interfaces.rest.transform;

import pe.greenminds.ecomind.notifications.domain.model.commands.CreateNotificationCommand;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.notifications.interfaces.rest.resources.CreateNotificationResource;

public final class CreateNotificationCommandFromResourceAssembler {

  private CreateNotificationCommandFromResourceAssembler() {
  }

  // The recipient is the user of the token: nobody can fill the inbox of another user
  public static CreateNotificationCommand toCommandFromResource(
      Long recipientId, CreateNotificationResource resource) {
    return new CreateNotificationCommand(
        new RecipientId(recipientId),
        NotificationCategory.valueOf(resource.category()),
        resource.title(),
        resource.message());
  }
}
