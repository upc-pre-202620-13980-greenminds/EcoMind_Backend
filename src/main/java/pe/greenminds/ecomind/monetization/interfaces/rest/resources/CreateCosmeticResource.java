package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCosmeticResource(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 500) String description,
    @Positive int priceInGems,
    @NotBlank String type,
    @NotBlank @Size(max = 500) String imageUrl) {}
