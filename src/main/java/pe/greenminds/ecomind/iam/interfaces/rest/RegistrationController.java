package pe.greenminds.ecomind.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.iam.application.commandservices.RegistrationCommandService;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.PendingRegistrationResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.SubmitRegistrationResource;
import pe.greenminds.ecomind.iam.interfaces.rest.resources.VerifyEmailResource;
import pe.greenminds.ecomind.iam.interfaces.rest.transform.AuthenticationCommandFromResourceAssembler;
import pe.greenminds.ecomind.iam.interfaces.rest.transform.AuthenticationResourceAssembler;
import pe.greenminds.ecomind.shared.interfaces.rest.resources.ErrorResource;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Registration", description = "Account registration and email verification")
public class RegistrationController {

  private final RegistrationCommandService registrationCommandService;
  private final ResponseEntityAssembler responseEntityAssembler;

  public RegistrationController(
      RegistrationCommandService registrationCommandService,
      ResponseEntityAssembler responseEntityAssembler) {
    this.registrationCommandService = registrationCommandService;
    this.responseEntityAssembler = responseEntityAssembler;
  }

  @PostMapping("/sign-up")
  @Operation(
      summary = "Start a registration",
      description = "Stores the registration as pending and sends a six-digit verification code "
          + "to the email address. The account is created only after the email is verified. "
          + "Submitting again for the same email replaces the previous code.")
  @ApiResponse(responseCode = "201", description = "Registration pending verification",
      content = @Content(schema = @Schema(implementation = PendingRegistrationResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or invalid fields, or weak password",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "VALIDATION_ERROR", "message": "The request contains invalid data.",
               "details": "email: must be a well-formed email address"}""")))
  @ApiResponse(responseCode = "409", description = "The email already has an account",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "EMAIL_CONFLICT", "message": "This email address is already registered."}""")))
  public ResponseEntity<?> submitRegistration(
      @Valid @RequestBody SubmitRegistrationResource resource) {
    var command = AuthenticationCommandFromResourceAssembler.toCommandFromResource(resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        registrationCommandService.handle(command),
        (PendingRegistration pending) ->
            AuthenticationResourceAssembler.toResourceFromEntity(pending),
        HttpStatus.CREATED);
  }

  @PostMapping("/verify-email")
  @Operation(
      summary = "Verify the email and create the account",
      description = "Checks the code sent by email. The code is valid for 20 minutes, can be used "
          + "once and allows up to 5 attempts. When it is correct the account is created.")
  @ApiResponse(responseCode = "201", description = "Account created",
      content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class)))
  @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "409", description = "The email was registered in the meantime",
      content = @Content(schema = @Schema(implementation = ErrorResource.class)))
  @ApiResponse(responseCode = "422",
      description = "The code is wrong, expired or has too many attempts",
      content = @Content(schema = @Schema(implementation = ErrorResource.class),
          examples = @ExampleObject("""
              {"code": "VERIFICATION_CODE_INVALID",
               "message": "The verification code is not valid."}""")))
  public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailResource resource) {
    var command = AuthenticationCommandFromResourceAssembler.toCommandFromResource(resource);
    return responseEntityAssembler.toResponseEntityFromResult(
        registrationCommandService.handle(command),
        (Account account) -> AuthenticationResourceAssembler.toResourceFromEntity(account),
        HttpStatus.CREATED);
  }
}
