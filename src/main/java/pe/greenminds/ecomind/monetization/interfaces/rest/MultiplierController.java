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
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.CreateMultiplierResource;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.MultiplierResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@RequestMapping(value = "/api/v1/monetization/multipliers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Multipliers", description = "Multiplier catalog management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class MultiplierController {
  private final StoreQueryService queries;
  private final CatalogCommandService commands;

  public MultiplierController(StoreQueryService queries, CatalogCommandService commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @GetMapping
  @Operation(summary = "List active multipliers")
  public List<MultiplierResource> findAll() {
    return queries.getCatalog().multipliers().stream().map(MultiplierResource::from).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an active multiplier by ID")
  public MultiplierResource findById(@PathVariable UUID id) {
    return queries.findMultiplierById(id).map(MultiplierResource::from)
        .orElseThrow(() -> new IllegalArgumentException("Multiplier was not found"));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a multiplier")
  public MultiplierResource create(@Valid @RequestBody CreateMultiplierResource resource) {
    return MultiplierResource.from(commands.createMultiplier(
        resource.name(), resource.description(), resource.factor(), resource.durationMinutes(),
        resource.priceInGems()));
  }
}
