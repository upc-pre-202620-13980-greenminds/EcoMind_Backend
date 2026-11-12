package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.greenminds.ecomind.community.application.commandservices.EventImageCommandService;
import pe.greenminds.ecomind.community.domain.model.commands.UploadEventImageCommand;
import pe.greenminds.ecomind.community.interfaces.rest.transform.EventImageResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@Tag(name = "Event", description = "Community event management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(
        value = "/api/v1/Community/Events/Images",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class EventImageController {

    private final EventImageCommandService commands;
    private final ResponseEntityAssembler responses;

    public EventImageController(
            EventImageCommandService commands,
            ResponseEntityAssembler responses) {
        this.commands = commands;
        this.responses = responses;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {

        byte[] content;

        try {
            content = file.getBytes();
        } catch (IOException exception) {
            return responses.toResponseEntityFromResult(
                    Result.<Void, ApplicationError>failure(
                            ApplicationError.validationError(
                                    "Event image",
                                    "The uploaded file could not be read")),
                    value -> null,
                    HttpStatus.CREATED);
        }

        var command = new UploadEventImageCommand(
                principal.accountId(),
                content);

        return responses.toResponseEntityFromResult(
                commands.handle(command),
                EventImageResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED);
    }
}