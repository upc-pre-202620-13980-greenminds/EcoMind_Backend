package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementAwardResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/gamification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class AchievementController {
  private final AchievementQueryService queries;
  private final ResponseEntityAssembler responses;

  public AchievementController(AchievementQueryService queries, ResponseEntityAssembler responses) {
    this.queries = queries;
    this.responses = responses;
  }

  @GetMapping("/achievements")
  @Operation(summary = "Browse the achievement catalog", description = "Optional scope filter. Pages start at zero; maximum size is 100. Includes inactive definitions.")
  public List<AchievementResource> search(@RequestParam(required = false) AchievementScope scope,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return queries.search(scope, page, size).stream().map(AchievementResource::from).toList();
  }

  @GetMapping("/achievements/{achievementId}")
  @Operation(summary = "Get an achievement definition")
  public ResponseEntity<?> find(@PathVariable UUID achievementId) {
    return responses.toResponseEntityFromResult(queries.find(achievementId), AchievementResource::from, HttpStatus.OK);
  }

  @GetMapping("/me/achievements")
  @Operation(summary = "Get my awarded achievements", description = "Pages start at zero; maximum size is 100.")
  public List<AchievementAwardResource> forUser(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return queries.forUser(new UserId(principal.accountId()), page, size).stream().map(AchievementAwardResource::from).toList();
  }

  @GetMapping("/families/{familyId}/achievements")
  @Operation(summary = "Get a family's awarded achievements", description = "Requires current family membership. Pages start at zero; maximum size is 100.")
  public ResponseEntity<?> forFamily(@PathVariable Long familyId,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return responses.toResponseEntityFromResult(
        queries.forFamily(new FamilyId(familyId), new UserId(principal.accountId()), page, size),
        awards -> awards.stream().map(AchievementAwardResource::from).toList(), HttpStatus.OK);
  }
}
