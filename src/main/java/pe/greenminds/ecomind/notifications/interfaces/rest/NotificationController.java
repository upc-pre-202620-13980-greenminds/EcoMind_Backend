package pe.greenminds.ecomind.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.notifications.application.commandservices.NotificationCommandService;
import pe.greenminds.ecomind.notifications.application.queryservices.NotificationQueryService;
import pe.greenminds.ecomind.notifications.domain.model.aggregates.Notification;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkAllNotificationsAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetNotificationsByRecipientQuery;
import pe.greenminds.ecomind.notifications.domain.model.queries.GetUnreadCountQuery;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.RecipientId;
import pe.greenminds.ecomind.notifications.interfaces.rest.resources.CreateNotificationResource;
import pe.greenminds.ecomind.notifications.interfaces.rest.resources.NotificationResource;
import pe.greenminds.ecomind.notifications.interfaces.rest.resources.UnreadCountResource;
import pe.greenminds.ecomind.notifications.interfaces.rest.transform.CreateNotificationCommandFromResourceAssembler;
import pe.greenminds.ecomind.notifications.interfaces.rest.transform.NotificationResourceAssembler;

/** Notification inbox of the authenticated user (TS-016). The user always comes from the token. */
@RestController
@RequestMapping(value = "/api/v1/notification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notification inbox of the authenticated user")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class NotificationController {

  private final NotificationCommandService notificationCommandService;
  private final NotificationQueryService notificationQueryService;
  private final NotificationResourceAssembler notificationResourceAssembler;
  private final ResponseEntityAssembler responseEntityAssembler;

  public NotificationController(
      NotificationCommandService notificationCommandService,
      NotificationQueryService notificationQueryService,
      NotificationResourceAssembler notificationResourceAssembler,
      ResponseEntityAssembler responseEntityAssembler) {
    this.notificationCommandService = notificationCommandService;
    this.notificationQueryService = notificationQueryService;
    this.notificationResourceAssembler = notificationResourceAssembler;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @GetMapping
  @Operation(
      summary = "List my notifications",
      description = "Returns the notifications of the authenticated user, the newest first. The "
          + "texts of the generated notifications follow the Accept-Language header.")
  @ApiResponse(responseCode = "200", description = "Notifications found",
      content = @Content(
          array = @ArraySchema(schema = @Schema(implementation = NotificationResource.class)),
          examples = @ExampleObject("""
              [{"id": 15, "category": "COMMUNITY_ACTIVITY", "type": "FRIEND_REQUEST_ACCEPTED",
                "title": "Friend request accepted",
                "message": "Ana Torres accepted your friend request.",
                "read": false, "createdAt": "2026-11-11T15:30:00Z"}]""")))
  public ResponseEntity<List<NotificationResource>> getNotifications(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    List<Notification> notifications =
        notificationQueryService.handle(
            new GetNotificationsByRecipientQuery(new RecipientId(principal.accountId())));
    return ResponseEntity.ok(
        notifications.stream().map(notificationResourceAssembler::toResourceFromEntity).toList());
  }

  @GetMapping("/unread-count")
  @Operation(
      summary = "Count my unread notifications",
      description = "Returns how many notifications of the authenticated user are not read.")
  @ApiResponse(responseCode = "200", description = "Unread notifications counted",
      content = @Content(schema = @Schema(implementation = UnreadCountResource.class),
          examples = @ExampleObject("""
              {"count": 3}""")))
  public ResponseEntity<UnreadCountResource> getUnreadCount(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    long count =
        notificationQueryService.handle(
            new GetUnreadCountQuery(new RecipientId(principal.accountId())));
    return ResponseEntity.ok(new UnreadCountResource(count));
  }

  @PostMapping
  @Operation(
      summary = "Create a notification for myself",
      description = "Creates a notification, such as a reminder, for the authenticated user only. "
          + "Other bounded contexts create their notifications through the application service.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(
          schema = @Schema(implementation = CreateNotificationResource.class),
          examples = @ExampleObject("""
              {"category": "LEARNING_REMINDERS",
               "title": "Pending material: Recycling essentials",
               "message": "2 days left to review it."}""")))
  @ApiResponse(responseCode = "201", description = "Notification created",
      content = @Content(schema = @Schema(implementation = NotificationResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing fields, too long texts or an unknown "
      + "category",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> createNotification(
      @Valid @RequestBody CreateNotificationResource resource,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        CreateNotificationCommandFromResourceAssembler.toCommandFromResource(
            principal.accountId(), resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        notificationCommandService.handle(command),
        notificationResourceAssembler::toResourceFromEntity,
        HttpStatus.CREATED);
  }

  @PatchMapping("/{notificationId}/read")
  @Operation(
      summary = "Mark a notification as read",
      description = "Marks one notification of the authenticated user as read.")
  @ApiResponse(responseCode = "200", description = "Notification marked as read",
      content = @Content(schema = @Schema(implementation = NotificationResource.class)))
  @ApiResponse(responseCode = "403", description = "The notification belongs to another user",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "NOTIFICATION_ACCESS_FORBIDDEN",
               "message": "You can only manage your own notifications."}""")))
  @ApiResponse(responseCode = "404", description = "The notification does not exist",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "NOTIFICATION_NOT_FOUND",
               "message": "The notification was not found."}""")))
  public ResponseEntity<?> markAsRead(
      @PathVariable Long notificationId,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        new MarkNotificationAsReadCommand(new RecipientId(principal.accountId()), notificationId);
    return responseEntityAssembler.toResponseEntityFromResult(
        notificationCommandService.handle(command),
        notificationResourceAssembler::toResourceFromEntity,
        HttpStatus.OK);
  }

  @PatchMapping("/read-all")
  @Operation(
      summary = "Mark all my notifications as read",
      description = "Marks every unread notification of the authenticated user as read.")
  @ApiResponse(responseCode = "204", description = "Notifications marked as read",
      content = @Content)
  public ResponseEntity<?> markAllAsRead(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command = new MarkAllNotificationsAsReadCommand(new RecipientId(principal.accountId()));
    return responseEntityAssembler.toResponseEntityFromResult(
        notificationCommandService.handle(command), (Integer updated) -> null,
        HttpStatus.NO_CONTENT);
  }
}
