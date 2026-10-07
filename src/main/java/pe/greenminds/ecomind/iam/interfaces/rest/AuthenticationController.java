package pe.greenminds.ecomind.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.iam.application.commandservices.AuthenticationCommandService;
import pe.greenminds.ecomind.iam.application.commandservices.SignInResult;
import pe.greenminds.ecomind.iam.application.queryservices.CurrentAuthenticatedUserService;
import pe.greenminds.ecomind.iam.domain.model.queries.GetCurrentAuthenticatedUserQuery;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.AuthenticationResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.SignInResource;
import pe.greenminds.ecomind.iam.interfaces.rest.transform.AuthenticationCommandFromResourceAssembler;
import pe.greenminds.ecomind.iam.interfaces.rest.transform.AuthenticationResourceAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Sign-in and identity of the authenticated account")
public class AuthenticationController {

  private final AuthenticationCommandService authenticationCommandService;
  private final CurrentAuthenticatedUserService currentAuthenticatedUserService;
  private final ResponseEntityAssembler responseEntityAssembler;

  public AuthenticationController(
      AuthenticationCommandService authenticationCommandService,
      CurrentAuthenticatedUserService currentAuthenticatedUserService,
      ResponseEntityAssembler responseEntityAssembler) {
    this.authenticationCommandService = authenticationCommandService;
    this.currentAuthenticatedUserService = currentAuthenticatedUserService;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @PostMapping("/sign-in")
  @Operation(
      summary = "Sign in",
      description = "Validates the credentials and returns a signed access token. The error is "
          + "the same whether the email or the password is wrong.")
  @ApiResponse(responseCode = "200", description = "Authenticated",
      content = @Content(schema = @Schema(implementation = AuthenticationResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "401", description = "Invalid credentials",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "INVALID_CREDENTIALS", "message": "The email or password is incorrect."}""")))
  public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
    var command = AuthenticationCommandFromResourceAssembler.toCommandFromResource(resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        authenticationCommandService.handle(command),
        (SignInResult result) -> AuthenticationResourceAssembler.toResourceFromEntity(result),
        HttpStatus.OK);
  }

  @GetMapping("/me")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
  @Operation(
      summary = "Get the authenticated account",
      description = "Returns the identity of the account that owns the access token.")
  @ApiResponse(responseCode = "200", description = "Identity of the account",
      content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class)))
  @ApiResponse(responseCode = "401", description = "Missing, invalid or expired access token",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "UNAUTHORIZED", "message": "Authentication is required to access this resource."}""")))
  public ResponseEntity<?> getCurrentAuthenticatedUser(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var query = new GetCurrentAuthenticatedUserQuery(new AccountId(principal.accountId()));
    return responseEntityAssembler.toResponseEntityFromResult(
        currentAuthenticatedUserService.handle(query),
        (AuthenticatedUser user) -> AuthenticationResourceAssembler.toResourceFromEntity(user),
        HttpStatus.OK);
  }
}
