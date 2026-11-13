package pe.greenminds.ecomind.learning.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.learning.application.TutorialService;
import pe.greenminds.ecomind.learning.interfaces.rest.resources.TutorialProgressResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@RequestMapping("/api/v1/tutorials")
@Tag(name = "Learning Tutorial", description = "Sequential first-session tutorial")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class TutorialController {
  private final TutorialService service;
  public TutorialController(TutorialService service) { this.service = service; }

  @GetMapping("/progress")
  public TutorialProgressResource progress(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.progress(principal.accountId());
  }

  @PostMapping("/progress/step")
  public TutorialProgressResource completeStep(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.completeStep(principal.accountId());
  }

  @PostMapping("/progress/complete")
  public TutorialProgressResource complete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.complete(principal.accountId());
  }
}
