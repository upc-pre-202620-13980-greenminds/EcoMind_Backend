package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.monetization.application.commandservices.GemPurchaseCommandService;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.*;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@RequestMapping(value = "/api/v1/monetization/me/gem-purchases", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class GemPurchaseController {
  private final GemPurchaseCommandService purchases;

  public GemPurchaseController(GemPurchaseCommandService purchases) {
    this.purchases = purchases;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Start a gem package checkout", tags = "Gem Purchases")
  public GemPurchaseResource checkout(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody CreateGemPurchaseResource resource) {
    return GemPurchaseResource.from(purchases.checkout(
        principal.accountId(), resource.packageId(), resource.paymentMethod(), resource.requestId()));
  }

  @PostMapping("/{purchaseId}/payment")
  @Operation(summary = "Pay a pending gem package checkout", tags = "Gem Purchases")
  public GemPurchaseResource pay(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @PathVariable UUID purchaseId,
      @Valid @RequestBody PayGemPurchaseResource resource) {
    return GemPurchaseResource.from(
        purchases.pay(principal.accountId(), purchaseId, resource.sourceToken(), resource.email()));
  }

  @GetMapping
  @Operation(summary = "Get my gem package purchases", tags = "Gem Purchases")
  public List<GemPurchaseResource> findMine(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    return purchases.findMine(principal.accountId()).stream().map(GemPurchaseResource::from).toList();
  }

  @GetMapping("/{purchaseId}")
  @Operation(summary = "Get one of my gem package purchases by ID", tags = "Gem Purchases")
  public GemPurchaseResource findMineById(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @PathVariable UUID purchaseId) {
    return GemPurchaseResource.from(purchases.findMineById(principal.accountId(), purchaseId));
  }
}
