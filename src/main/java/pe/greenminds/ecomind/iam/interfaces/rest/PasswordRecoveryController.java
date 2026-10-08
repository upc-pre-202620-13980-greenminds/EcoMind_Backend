package pe.greenminds.ecomind.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.iam.application.commandservices.PasswordRecoveryCommandService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.ConfirmPasswordRecoveryResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.PasswordRecoveryResource;
import pe.greenminds.ecomind.iam.interfaces.rest.transform.AuthenticationCommandFromResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.MessageResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(
    value = "/api/v1/authentication/password-recovery",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Password Recovery", description = "Recovery of a forgotten password")
public class PasswordRecoveryController {

  private final PasswordRecoveryCommandService passwordRecoveryCommandService;
  private final ResponseEntityAssembler responseEntityAssembler;
  private final MessageSource messageSource;

  public PasswordRecoveryController(
      PasswordRecoveryCommandService passwordRecoveryCommandService,
      ResponseEntityAssembler responseEntityAssembler,
      MessageSource messageSource) {
    this.passwordRecoveryCommandService = passwordRecoveryCommandService;
    this.responseEntityAssembler = responseEntityAssembler;
    this.messageSource = messageSource;
  }

  @PostMapping("/request")
  @Operation(
      summary = "Request a password recovery",
      description = "Sends a recovery link to the email address when it belongs to an account. "
          + "The response is the same for any email, so it does not reveal which ones are "
          + "registered.")
  @ApiResponse(responseCode = "200", description = "Request accepted",
      content = @Content(schema = @Schema(implementation = MessageResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or malformed email",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  public ResponseEntity<?> requestPasswordRecovery(
      @Valid @RequestBody PasswordRecoveryResource resource) {
    var command = AuthenticationCommandFromResourceAssembler.toCommandFromResource(resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        passwordRecoveryCommandService.handle(command),
        (EmailAddress email) -> new MessageResource(
            messageSource.getMessage(
                "iam.password-recovery.requested", null, LocaleContextHolder.getLocale())),
        HttpStatus.OK);
  }

  @PostMapping("/confirm")
  @Operation(
      summary = "Set a new password",
      description = "Consumes the token of the recovery link, which is valid for 30 minutes and "
          + "can be used once, and replaces the password of the account.")
  @ApiResponse(responseCode = "204", description = "Password updated", content = @Content)
  @ApiResponse(responseCode = "400", description = "Missing fields or weak password",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "PASSWORD_POLICY_VIOLATION",
               "message": "The password must have 8 to 72 characters, with at least one letter and one digit."}""")))
  @ApiResponse(responseCode = "422", description = "The token is wrong, expired or already used",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "PASSWORD_RESET_TOKEN_INVALID",
               "message": "The recovery link is not valid or has expired."}""")))
  public ResponseEntity<?> confirmPasswordRecovery(
      @Valid @RequestBody ConfirmPasswordRecoveryResource resource) {
    var command = AuthenticationCommandFromResourceAssembler.toCommandFromResource(resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        passwordRecoveryCommandService.handle(command),
        (AccountId accountId) -> null,
        HttpStatus.NO_CONTENT);
  }
}
