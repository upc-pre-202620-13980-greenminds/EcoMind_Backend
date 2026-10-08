package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import java.util.List;

public record InventoryResource(
    List<OwnedCosmeticResource> cosmetics,
    List<OwnedProtectorResource> protectors,
    List<OwnedMultiplierResource> multipliers) {}
