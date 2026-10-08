package pe.greenminds.ecomind.quests.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.quests.application.commandservices.CollabQuestMemberCommandService;
import pe.greenminds.ecomind.quests.application.queryservices.CollabQuestMemberQueryService;
import pe.greenminds.ecomind.quests.domain.model.commands.AcceptCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.DeclineCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.LeaveCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.RemoveCollabQuestMemberCommand;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMemberByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersBySessionQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetCollabQuestMembersByUserQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabMemberStatus;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.CollabQuestMemberResource;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.InviteCollabQuestMemberResource;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.CollabQuestMemberResourceFromEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.InviteCollabQuestMemberCommandFromResourceAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.quests.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;

import java.util.List;

@RestController
@RequestMapping(
        value = "/api/v1/collaborative-quest-members",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Collaborative Quest Members", description = "Collaborative quest member endpoints")
public class CollabQuestMemberController {
    private final CollabQuestMemberCommandService collabQuestMemberCommandService;
    private final CollabQuestMemberQueryService collabQuestMemberQueryService;

    public CollabQuestMemberController(
            CollabQuestMemberCommandService collabQuestMemberCommandService,
            CollabQuestMemberQueryService collabQuestMemberQueryService
    ) {
        this.collabQuestMemberCommandService = collabQuestMemberCommandService;
        this.collabQuestMemberQueryService = collabQuestMemberQueryService;
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "Get a collaborative quest member by ID")
    public ResponseEntity<?> getById(@PathVariable Long memberId) {
        var member = collabQuestMemberQueryService.handle(
                new GetCollabQuestMemberByIdQuery(memberId));
        if (member.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("CollabQuestMember", memberId.toString()));
        }
        return ResponseEntity.ok(
                CollabQuestMemberResourceFromEntityAssembler.toResourceFromEntity(member.get()));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get all members of a collaborative quest session")
    public ResponseEntity<List<CollabQuestMemberResource>> getBySession(
            @PathVariable Long sessionId) {
        var resources = collabQuestMemberQueryService.handle(
                        new GetCollabQuestMembersBySessionQuery(sessionId))
                .stream().map(CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get collaborative quest memberships or invitations for a user")
    public ResponseEntity<List<CollabQuestMemberResource>> getByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) CollabMemberStatus status) {
        var resources = collabQuestMemberQueryService.handle(
                        new GetCollabQuestMembersByUserQuery(userId, status))
                .stream().map(CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    @Operation(
            summary = "Invite a user to a collaborative quest session",
            description = """
                    Creates a pending participant member invitation.
                    The invited user must be an accepted friend or family member.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Collaborative quest member invited successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestMemberResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Session or user not found"),
            @ApiResponse(responseCode = "409", description = "Invitation conflicts with existing membership"),
            @ApiResponse(responseCode = "422", description = "Invitation is not allowed")
    })
    public ResponseEntity<?> inviteMember(
            @Valid @RequestBody InviteCollabQuestMemberResource resource
    ) {
        var command =
                InviteCollabQuestMemberCommandFromResourceAssembler.toCommandFromResource(
                        resource
                );
        var result = collabQuestMemberCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{memberId}/accept")
    @Operation(summary = "Accept a collaborative quest invitation")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collaborative quest invitation accepted successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestMemberResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "409", description = "User already accepted this quest elsewhere"),
            @ApiResponse(responseCode = "422", description = "Invitation cannot be accepted")
    })
    public ResponseEntity<?> accept(@PathVariable Long memberId) {
        var result = collabQuestMemberCommandService.handle(
                new AcceptCollabQuestMemberCommand(memberId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{memberId}/decline")
    @Operation(summary = "Decline a collaborative quest invitation")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collaborative quest invitation declined successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestMemberResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "422", description = "Invitation cannot be declined")
    })
    public ResponseEntity<?> decline(@PathVariable Long memberId) {
        var result = collabQuestMemberCommandService.handle(
                new DeclineCollabQuestMemberCommand(memberId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{memberId}/leave")
    @Operation(summary = "Leave a collaborative quest session")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collaborative quest member left successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestMemberResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Member or session not found"),
            @ApiResponse(responseCode = "422", description = "Member cannot leave")
    })
    public ResponseEntity<?> leave(@PathVariable Long memberId) {
        var result = collabQuestMemberCommandService.handle(
                new LeaveCollabQuestMemberCommand(memberId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PatchMapping("/{memberId}/remove")
    @Operation(summary = "Remove an invited collaborative quest member")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Collaborative quest member removed successfully",
                    content = @Content(schema = @Schema(implementation = CollabQuestMemberResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Member or session not found"),
            @ApiResponse(responseCode = "422", description = "Member cannot be removed")
    })
    public ResponseEntity<?> remove(
            @PathVariable Long memberId,
            @RequestParam Long ownerUserId
    ) {
        var result = collabQuestMemberCommandService.handle(
                new RemoveCollabQuestMemberCommand(memberId, ownerUserId)
        );

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CollabQuestMemberResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}
