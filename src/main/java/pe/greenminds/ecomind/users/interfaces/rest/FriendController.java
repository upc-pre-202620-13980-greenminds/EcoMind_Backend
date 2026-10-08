package pe.greenminds.ecomind.users.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.users.application.commandservices.FriendshipCommandService;
import pe.greenminds.ecomind.users.application.queryservices.FriendshipQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFriendshipsQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFriendsByUserQuery;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FriendResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.RespondFriendRequestResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.SendFriendRequestResource;
import pe.greenminds.ecomind.users.interfaces.rest.transform.FriendResourceFromEntityAssembler;
import pe.greenminds.ecomind.users.interfaces.rest.transform.FriendshipCommandFromResourceAssembler;

@RestController
@RequestMapping(value = "/api/v1/friend", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Friends", description = "Friend requests between users")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class FriendController {

  private final FriendshipCommandService friendshipCommandService;
  private final FriendshipQueryService friendshipQueryService;
  private final ResponseEntityAssembler responseEntityAssembler;

  public FriendController(
      FriendshipCommandService friendshipCommandService,
      FriendshipQueryService friendshipQueryService,
      ResponseEntityAssembler responseEntityAssembler) {
    this.friendshipCommandService = friendshipCommandService;
    this.friendshipQueryService = friendshipQueryService;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @GetMapping
  @Operation(
      summary = "List friend requests",
      description = "Returns every friend request with its state, or only the ones a user "
          + "sent or received when user_id is sent.")
  @ApiResponse(responseCode = "200", description = "Friend requests found",
      content = @Content(
          array = @ArraySchema(schema = @Schema(implementation = FriendResource.class))))
  public ResponseEntity<List<FriendResource>> getFriends(
      @Parameter(description = "Id of the user who sent or received the requests", example = "1")
      @RequestParam(name = "user_id", required = false) Long userId) {
    List<Friendship> friendships =
        userId == null
            ? friendshipQueryService.handle(new GetAllFriendshipsQuery())
            : friendshipQueryService.handle(new GetFriendsByUserQuery(new UserId(userId)));
    return ResponseEntity.ok(
        friendships.stream().map(FriendResourceFromEntityAssembler::toResourceFromEntity).toList());
  }

  @PostMapping
  @Operation(
      summary = "Send a friend request",
      description = "Sends a friend request from the authenticated user to another user. A "
          + "request cannot be sent to oneself or repeated while one is pending or accepted; "
          + "after a rejection it can be sent again once 7 days have passed.")
  @ApiResponse(responseCode = "201", description = "Friend request sent",
      content = @Content(schema = @Schema(implementation = FriendResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "404", description = "The receiver does not exist",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "409", description = "A request already exists between the users",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FRIENDSHIP_CONFLICT",
               "message": "There is already a friend request between you and this user."}""")))
  @ApiResponse(responseCode = "422",
      description = "The request is to oneself or was rejected less than 7 days ago",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> sendFriendRequest(
      @Valid @RequestBody SendFriendRequestResource resource,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        FriendshipCommandFromResourceAssembler.toCommandFromResource(
            principal.accountId(), resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        friendshipCommandService.handle(command),
        (Friendship friendship) ->
            FriendResourceFromEntityAssembler.toResourceFromEntity(friendship),
        HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Answer a friend request",
      description = "Accepts or rejects a pending friend request. Only the user who received "
          + "it can answer.")
  @ApiResponse(responseCode = "200", description = "Friend request answered",
      content = @Content(schema = @Schema(implementation = FriendResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "403", description = "The requester did not receive the request",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FRIEND_REQUEST_ACCESS_FORBIDDEN",
               "message": "Only the user who received the friend request can answer it."}""")))
  @ApiResponse(responseCode = "404", description = "The friend request does not exist",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "422", description = "The request was already answered",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> respondFriendRequest(
      @PathVariable Long id,
      @Valid @RequestBody RespondFriendRequestResource resource,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        FriendshipCommandFromResourceAssembler.toCommandFromResource(
            principal.accountId(), id, resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        friendshipCommandService.handle(command),
        (Friendship friendship) ->
            FriendResourceFromEntityAssembler.toResourceFromEntity(friendship),
        HttpStatus.OK);
  }
}
