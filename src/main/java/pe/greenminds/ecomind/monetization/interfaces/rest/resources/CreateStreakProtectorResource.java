package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateStreakProtectorResource(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 500) String description,
    @Positive int priceInGems) {}
