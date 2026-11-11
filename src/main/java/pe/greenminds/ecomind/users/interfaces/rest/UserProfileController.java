package pe.greenminds.ecomind.users.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.interfaces.acl.GamificationContextFacade;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.greenminds.ecomind.users.application.commandservices.ProfileCommandService;
import pe.greenminds.ecomind.users.application.queryservices.ProfileQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllUserProfilesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetUserProfileQuery;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UpdateUserProfileResource;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UserProfileResource;
import pe.greenminds.ecomind.users.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import pe.greenminds.ecomind.users.interfaces.rest.transform.UserProfileResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Profiles", description = "Profiles of the users and their progress")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class UserProfileController {

    private final ProfileCommandService profileCommandService;
    private final GamificationContextFacade gamification;
    private final ProfileQueryService profileQueryService;
    private final ResponseEntityAssembler responseEntityAssembler;
    private final ErrorResponseAssembler errorResponseAssembler;

    public UserProfileController(
            ProfileCommandService profileCommandService,
            ProfileQueryService profileQueryService,
            ResponseEntityAssembler responseEntityAssembler,
            ErrorResponseAssembler errorResponseAssembler,
            GamificationContextFacade gamification) {
        this.profileCommandService = profileCommandService;
        this.gamification = gamification;
        this.profileQueryService = profileQueryService;
        this.responseEntityAssembler = responseEntityAssembler;
        this.errorResponseAssembler = errorResponseAssembler;
    }

    @GetMapping
    @Operation(
            summary = "List user profiles",
            description =
                    "Returns every registered profile. The invitation code is only filled in "
                            + "for the profile of the authenticated user.")
    @ApiResponse(
            responseCode = "200",
            description = "Profiles found",
            content =
                    @Content(
                            array =
                                    @ArraySchema(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    UserProfileResource.class))))
    public ResponseEntity<List<UserProfileResource>> getAllUserProfiles(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        UserId viewer = viewerOf(principal);
        var resources =
                profileQueryService.handle(new GetAllUserProfilesQuery()).stream()
                        .map(profile -> toProfileResource(profile, viewer))
                        .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a user profile",
            description =
                    "Returns the name, role, streak, ecopoints, gem balance and equipped "
                            + "cosmetic of a user. The invitation code is only returned when the "
                            + "profile belongs to the authenticated user.")
    @ApiResponse(
            responseCode = "200",
            description = "Profile found",
            content = @Content(schema = @Schema(implementation = UserProfileResource.class)))
    @ApiResponse(
            responseCode = "404",
            description = "The user has no profile",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResource.class),
                            examples =
                                    @ExampleObject(
                                            """
                                            {"code": "USER_PROFILE_NOT_FOUND", "message": "The user profile was not found."}\
                                            """)))
    public ResponseEntity<?> getUserProfile(
            @PathVariable Long id, @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        UserId viewer = viewerOf(principal);
        return profileQueryService
                .handle(new GetUserProfileQuery(new UserId(id)))
                .<ResponseEntity<?>>map(
                        profile -> ResponseEntity.ok(toProfileResource(profile, viewer)))
                .orElseGet(
                        () ->
                                errorResponseAssembler.toErrorResponseFromApplicationError(
                                        ApplicationError.notFound(
                                                "USER_PROFILE", String.valueOf(id))));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update the name and the progress of a profile",
            description =
                    "Replaces the name, streak, last streak date, ecopoints and gem balance. "
                            + "The name is required and has 2 to 120 characters. A user "
                            + "can only update their own profile.")
    @ApiResponse(
            responseCode = "200",
            description = "Profile updated",
            content = @Content(schema = @Schema(implementation = UserProfileResource.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Missing or negative values, or a name outside 2 to 120 characters",
            content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    @ApiResponse(
            responseCode = "403",
            description = "The profile belongs to another user",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResource.class),
                            examples =
                                    @ExampleObject(
                                            """
                                            {"code": "PROFILE_ACCESS_FORBIDDEN",
                                             "message": "You can only update your own profile."}\
                                            """)))
    @ApiResponse(
            responseCode = "404",
            description = "The user has no profile",
            content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileResource resource,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        var command =
                UpdateProfileCommandFromResourceAssembler.toCommandFromResource(
                        principal.accountId(), id, resource);
        return responseEntityAssembler.toResponseEntityFromResult(
                profileCommandService.handle(command),
                (UserProfile profile) -> toProfileResource(profile, viewerOf(principal)),
                HttpStatus.OK);
    }

    private static UserId viewerOf(AuthenticatedUserPrincipal principal) {
        return principal == null ? null : new UserId(principal.accountId());
    }

    private UserProfileResource toProfileResource(UserProfile profile, UserId viewer) {
        var progress = gamification.getUserProgress(profile.getUserId().value());
        return new UserProfileResource(
                profile.getUserId().value(),
                profile.getName(),
                profile.getSocialRole().name(),
                progress.currentStreak(),
                progress.lastActivityDate(),
                progress.ecopoints(),
                profile.getGemBalance(),
                profile.getEquippedCosmeticId(),
                UserProfileResourceFromEntityAssembler.invitationCodeFor(profile, viewer));
    }
}
