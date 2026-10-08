package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.monetization.application.queryservices.StoreQueryService;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.StoreCatalogResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@RequestMapping(value = "/api/v1/monetization/store", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class StoreController {
  private final StoreQueryService queries;

  public StoreController(StoreQueryService queries) {
    this.queries = queries;
  }

  @GetMapping
  @Operation(
      summary = "Browse the active EcoMind store catalog",
      tags = "Store Catalog")
  public StoreCatalogResource getCatalog() {
    return StoreCatalogResource.from(queries.getCatalog());
  }
}
