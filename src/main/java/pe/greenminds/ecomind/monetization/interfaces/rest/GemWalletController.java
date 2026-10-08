package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.greenminds.ecomind.monetization.application.queryservices.GemWalletQueryService;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.GemMovementResource;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.GemWalletResource;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@RequestMapping(value = "/api/v1/monetization/me/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class GemWalletController {
  private final GemWalletQueryService queries;

  public GemWalletController(GemWalletQueryService queries) {
    this.queries = queries;
  }

  @GetMapping
  @Operation(summary = "Get my gem balance", tags = "Gem Wallet")
  public GemWalletResource getMyWallet(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return GemWalletResource.from(queries.getWallet(principal.accountId()));
  }

  @GetMapping("/movements")
  @Operation(summary = "Get my 100 most recent gem movements", tags = "Gem Movements")
  public List<GemMovementResource> getMyMovements(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return queries.getRecentMovements(principal.accountId()).stream()
        .map(GemMovementResource::from).toList();
  }
}
