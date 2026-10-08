package pe.greenminds.ecomind.monetization.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.monetization.application.internal.commandservices.InventoryCommandService;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.*;
import pe.greenminds.ecomind.monetization.interfaces.rest.resources.*;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

@RestController
@RequestMapping(value = "/api/v1/monetization/me", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class InventoryController {
  private final InventoryCommandService commands;
  private final UserCosmeticPersistenceRepository cosmetics;
  private final ProtectorInventoryPersistenceRepository protectors;
  private final UserMultiplierPersistenceRepository multipliers;

  public InventoryController(
      InventoryCommandService commands,
      UserCosmeticPersistenceRepository cosmetics,
      ProtectorInventoryPersistenceRepository protectors,
      UserMultiplierPersistenceRepository multipliers) {
    this.commands = commands;
    this.cosmetics = cosmetics;
    this.protectors = protectors;
    this.multipliers = multipliers;
  }

  @PostMapping("/cosmetics/purchases")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Buy a cosmetic with gems", tags = "User Cosmetics")
  public OwnedCosmeticResource buyCosmetic(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody BuyItemResource resource) {
    return OwnedCosmeticResource.from(
        commands.buyCosmetic(principal.accountId(), resource.itemId(), resource.requestId()));
  }

  @PutMapping("/cosmetics/{id}/equipped")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Equip an owned cosmetic", tags = "User Cosmetics")
  public void equip(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID id) {
    commands.equipCosmetic(principal.accountId(), id, true);
  }

  @DeleteMapping("/cosmetics/{id}/equipped")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Unequip an owned cosmetic", tags = "User Cosmetics")
  public void unequip(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID id) {
    commands.equipCosmetic(principal.accountId(), id, false);
  }

  @PostMapping("/protectors/purchases")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Buy a streak protector with gems", tags = "User Streak Protectors")
  public OwnedProtectorResource buyProtector(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody BuyItemResource resource) {
    return OwnedProtectorResource.from(
        commands.buyProtector(principal.accountId(), resource.itemId(), resource.requestId()));
  }

  @PostMapping("/multipliers/purchases")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Buy and activate an XP multiplier with gems", tags = "User Multipliers")
  public OwnedMultiplierResource buyMultiplier(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
      @Valid @RequestBody BuyItemResource resource) {
    return OwnedMultiplierResource.from(
        commands.buyMultiplier(principal.accountId(), resource.itemId(), resource.requestId()));
  }

  @GetMapping("/inventory")
  @Operation(summary = "Get my virtual inventory", tags = "Inventory")
  public InventoryResource inventory(
      @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
    Long userId = principal.accountId();
    return new InventoryResource(
        cosmetics.findByUserId(userId).stream().map(OwnedCosmeticResource::from).toList(),
        protectors.findByUserId(userId).stream().map(OwnedProtectorResource::from).toList(),
        multipliers.findByUserIdOrderByStartsAtDesc(userId).stream()
            .map(OwnedMultiplierResource::from).toList());
  }
}
