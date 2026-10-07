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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.users.application.commandservices.FamilyCommandService;
import pe.greenminds.ecomind.users.application.queryservices.FamilyQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.commands.RemoveFamilyMemberCommand;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFamiliesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyMembersByUserQuery;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.AddFamilyMemberResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.CreateFamilyResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FamilyMemberResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.FamilyResource;
import pe.greenminds.ecomind.users.interfaces.rest.transform.FamilyCommandFromResourceAssembler;
import pe.greenminds.ecomind.users.interfaces.rest.transform.FamilyResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Families", description = "Family groups and their members")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class FamilyController {

  private final FamilyCommandService familyCommandService;
  private final FamilyQueryService familyQueryService;
  private final ResponseEntityAssembler responseEntityAssembler;

  public FamilyController(
      FamilyCommandService familyCommandService,
      FamilyQueryService familyQueryService,
      ResponseEntityAssembler responseEntityAssembler) {
    this.familyCommandService = familyCommandService;
    this.familyQueryService = familyQueryService;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @GetMapping("/family")
  @Operation(summary = "List families", description = "Returns every family with its members.")
  @ApiResponse(responseCode = "200", description = "Families found",
      content = @Content(
          array = @ArraySchema(schema = @Schema(implementation = FamilyResource.class))))
  public ResponseEntity<List<FamilyResource>> getAllFamilies() {
    var resources =
        familyQueryService.handle(new GetAllFamiliesQuery()).stream()
            .map(FamilyResourceFromEntityAssembler::toResourceFromEntity)
            .toList();
    return ResponseEntity.ok(resources);
  }

  @PostMapping("/family")
  @Operation(
      summary = "Create a family",
      description = "Creates a family whose first member is the authenticated user, with the "
          + "PARENT role. Only users registered as parents can create one, and a user can "
          + "belong to one family at a time.")
  @ApiResponse(responseCode = "201", description = "Family created",
      content = @Content(schema = @Schema(implementation = FamilyResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "409", description = "The user already belongs to a family",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FAMILY_MEMBERSHIP_CONFLICT",
               "message": "The user already belongs to a family."}""")))
  @ApiResponse(responseCode = "422", description = "The user is not registered as a parent",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FAMILY_CREATOR_NOT_PARENT",
               "message": "Only a parent can create a family."}""")))
  public ResponseEntity<?> createFamily(
      @Valid @RequestBody CreateFamilyResource resource,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        FamilyCommandFromResourceAssembler.toCommandFromResource(principal.accountId(), resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        familyCommandService.handle(command),
        (Family family) -> FamilyResourceFromEntityAssembler.toResourceFromEntity(family),
        HttpStatus.CREATED);
  }

  @GetMapping("/family_user")
  @Operation(
      summary = "List family members",
      description = "Returns the members of every family, or only the membership of one user "
          + "when user_id is sent.")
  @ApiResponse(responseCode = "200", description = "Members found",
      content = @Content(
          array = @ArraySchema(schema = @Schema(implementation = FamilyMemberResource.class))))
  public ResponseEntity<List<FamilyMemberResource>> getFamilyMembers(
      @Parameter(description = "Id of the user whose membership is requested", example = "8")
      @RequestParam(name = "user_id", required = false) Long userId) {
    if (userId == null) {
      var resources =
          familyQueryService.handle(new GetAllFamiliesQuery()).stream()
              .flatMap(
                  family ->
                      FamilyResourceFromEntityAssembler.toMemberResourcesFromEntity(family)
                          .stream())
              .toList();
      return ResponseEntity.ok(resources);
    }
    UserId id = new UserId(userId);
    var resources =
        familyQueryService
            .handle(new GetFamilyMembersByUserQuery(id))
            .flatMap(
                family ->
                    family
                        .findMemberByUser(id)
                        .map(
                            member ->
                                FamilyResourceFromEntityAssembler.toMemberResourceFromEntity(
                                    family, member)))
            .map(List::of)
            .orElseGet(List::of);
    return ResponseEntity.ok(resources);
  }

  @PostMapping("/family_user")
  @Operation(
      summary = "Add a member to a family",
      description = "Adds a user to the family with the given role. Only a parent of that "
          + "family can do it, and the user must not belong to another family.")
  @ApiResponse(responseCode = "201", description = "Member added",
      content = @Content(schema = @Schema(implementation = FamilyResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "403", description = "The requester is not a parent of the family",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FAMILY_ACCESS_FORBIDDEN",
               "message": "Only a parent of the family can manage its members."}""")))
  @ApiResponse(responseCode = "404", description = "The family or the user does not exist",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "409", description = "The user already belongs to a family",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> addFamilyMember(
      @Valid @RequestBody AddFamilyMemberResource resource,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command =
        FamilyCommandFromResourceAssembler.toCommandFromResource(principal.accountId(), resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        familyCommandService.handle(command),
        (Family family) -> FamilyResourceFromEntityAssembler.toResourceFromEntity(family),
        HttpStatus.CREATED);
  }

  @DeleteMapping("/family_user/{id}")
  @Operation(
      summary = "Remove a member from a family",
      description = "Removes the membership with the given id. Only a parent of that family "
          + "can do it, and a parent cannot remove themselves.")
  @ApiResponse(responseCode = "204", description = "Member removed", content = @Content)
  @ApiResponse(responseCode = "403", description = "The requester is not a parent of the family",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "404", description = "The membership does not exist",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "FAMILY_MEMBER_NOT_FOUND",
               "message": "The family member was not found."}""")))
  @ApiResponse(responseCode = "422", description = "A parent tried to remove themselves",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> removeFamilyMember(
      @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command = new RemoveFamilyMemberCommand(new UserId(principal.accountId()), id);
    return responseEntityAssembler.toResponseEntityFromResult(
        familyCommandService.handle(command), (Family family) -> null, HttpStatus.NO_CONTENT);
  }
}
