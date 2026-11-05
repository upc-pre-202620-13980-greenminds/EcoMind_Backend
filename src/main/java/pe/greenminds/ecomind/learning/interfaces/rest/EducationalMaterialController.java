package pe.greenminds.ecomind.learning.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.learning.application.LearningService;
import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.learning.interfaces.rest.resources.*;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@RequestMapping(value = "/api/v1/educational-materials", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Learning", description = "Educational materials, search and favorites")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class EducationalMaterialController {
  private final LearningService service;
  public EducationalMaterialController(LearningService service) { this.service = service; }

  @GetMapping
  @Operation(summary = "List and filter educational materials")
  public List<EducationalMaterialResource> findAll(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) MaterialCategory category,
      @RequestParam(required = false) MaterialType materialType,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) Boolean downloadable,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.search(principal.accountId(), query, category, materialType, language, downloadable);
  }

  @GetMapping("/search")
  @Operation(summary = "Search educational materials")
  public List<EducationalMaterialResource> search(
      @RequestParam(defaultValue = "") String title,
      @RequestParam(required = false) MaterialCategory category,
      @RequestParam(required = false) MaterialType materialType,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.search(principal.accountId(), title, category, materialType, null, null);
  }

  @GetMapping("/favorites")
  @Operation(summary = "List the current user's favorite materials")
  public List<EducationalMaterialResource> favorites(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.favorites(principal.accountId());
  }

  @GetMapping("/{materialId}")
  public EducationalMaterialResource findById(@PathVariable Long materialId,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.get(materialId, principal.accountId());
  }

  @PostMapping("/{materialId}/favorite")
  public Map<String, Boolean> toggleFavorite(@PathVariable Long materialId,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return Map.of("favorite", service.toggleFavorite(materialId, principal.accountId()));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public EducationalMaterialResource create(@Valid @RequestBody SaveEducationalMaterialResource request) {
    return service.create(request);
  }

  @PutMapping(value = "/{materialId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public EducationalMaterialResource update(@PathVariable Long materialId,
      @Valid @RequestBody SaveEducationalMaterialResource request,
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return service.update(materialId, request, principal.accountId());
  }

  @DeleteMapping("/{materialId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long materialId) { service.delete(materialId); }
}
