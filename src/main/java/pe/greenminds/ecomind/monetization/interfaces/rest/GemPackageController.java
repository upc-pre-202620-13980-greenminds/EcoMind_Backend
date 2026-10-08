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
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.CreateGemPackageResource;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.GemPackageResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@RequestMapping(value = "/api/v1/monetization/gem-packages", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gem Packages", description = "Gem package catalog management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class GemPackageController {
  private final StoreQueryService queries;
  private final CatalogCommandService commands;

  public GemPackageController(StoreQueryService queries, CatalogCommandService commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @GetMapping
  @Operation(summary = "List active gem packages")
  public List<GemPackageResource> findAll() {
    return queries.getCatalog().gemPackages().stream().map(GemPackageResource::from).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an active gem package by ID")
  public GemPackageResource findById(@PathVariable UUID id) {
    return queries.findGemPackageById(id).map(GemPackageResource::from)
        .orElseThrow(() -> new IllegalArgumentException("Gem package was not found"));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a gem package")
  public GemPackageResource create(@Valid @RequestBody CreateGemPackageResource resource) {
    return GemPackageResource.from(commands.createGemPackage(
        resource.name(), resource.gemAmount(), resource.price(), resource.currency()));
  }
}
