package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.monetization.application.commandservices.CatalogCommandService;
import pe.greenminds.ecomind.monetization.application.queryservices.StoreQueryService;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.CreateStreakProtectorResource;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.StreakProtectorResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@RequestMapping(value = "/api/v1/monetization/streak-protectors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Streak Protectors", description = "Streak protector catalog management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class StreakProtectorController {
  private final StoreQueryService queries;
  private final CatalogCommandService commands;

  public StreakProtectorController(StoreQueryService queries, CatalogCommandService commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @GetMapping
  @Operation(summary = "List active streak protectors")
  public List<StreakProtectorResource> findAll() {
    return queries.getCatalog().streakProtectors().stream()
        .map(StreakProtectorResource::from).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an active streak protector by ID")
  public StreakProtectorResource findById(@PathVariable UUID id) {
    return queries.findStreakProtectorById(id).map(StreakProtectorResource::from)
        .orElseThrow(() -> new IllegalArgumentException("Streak protector was not found"));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a streak protector")
  public StreakProtectorResource create(
      @Valid @RequestBody CreateStreakProtectorResource resource) {
    return StreakProtectorResource.from(commands.createStreakProtector(
        resource.name(), resource.description(), resource.priceInGems()));
  }
}
