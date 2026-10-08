package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.monetization.application.commandservices.CatalogCommandService;
import pe.greenminds.ecomind.monetization.application.queryservices.StoreQueryService;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.CosmeticResource;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.CreateCosmeticResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@RequestMapping(value = "/api/v1/monetization/cosmetics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cosmetics", description = "Cosmetic catalog management")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class CosmeticController {
  private final StoreQueryService queries;
  private final CatalogCommandService commands;

  public CosmeticController(StoreQueryService queries, CatalogCommandService commands) {
    this.queries = queries;
    this.commands = commands;
  }

  @GetMapping
  @Operation(summary = "List active cosmetics")
  public List<CosmeticResource> findAll() {
    return queries.getCatalog().cosmetics().stream().map(CosmeticResource::from).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an active cosmetic by ID")
  public CosmeticResource findById(@PathVariable UUID id) {
    return queries.findCosmeticById(id).map(CosmeticResource::from)
        .orElseThrow(() -> new IllegalArgumentException("Cosmetic was not found"));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a cosmetic")
  public CosmeticResource create(@Valid @RequestBody CreateCosmeticResource resource) {
    return CosmeticResource.from(commands.createCosmetic(
        resource.name(), resource.description(), resource.priceInGems(), resource.type(),
        resource.imageUrl()));
  }
}
