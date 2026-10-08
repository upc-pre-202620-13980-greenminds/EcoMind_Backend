package pe.greenminds.ecomind.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.iam.application.commandservices.SessionCommandService;
import pe.greenminds.ecomind.iam.domain.model.commands.LogoutCommand;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Session", description = "End of the session of the authenticated account")
public class SessionController {

  private final SessionCommandService sessionCommandService;
  private final ResponseEntityAssembler responseEntityAssembler;

  public SessionController(
      SessionCommandService sessionCommandService,
      ResponseEntityAssembler responseEntityAssembler) {
    this.sessionCommandService = sessionCommandService;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @PostMapping("/logout")
  @SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
  @Operation(
      summary = "Log out",
      description = "Confirms the end of the session. Access tokens are stateless: the client "
          + "must discard its token, which is not revoked on the server.")
  @ApiResponse(responseCode = "204", description = "Session finished", content = @Content)
  @ApiResponse(responseCode = "401", description = "Missing, invalid or expired access token",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "UNAUTHORIZED", "message": "Authentication is required to access this resource."}""")))
  public ResponseEntity<?> logout(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    var command = new LogoutCommand(new AccountId(principal.accountId()));
    return responseEntityAssembler.toResponseEntityFromResult(
        sessionCommandService.handle(command),
        (AccountId accountId) -> null,
        HttpStatus.NO_CONTENT);
  }
}
