package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateMultiplierResource(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 500) String description,
    @NotNull @DecimalMin(value = "1.0", inclusive = false) BigDecimal factor,
    @Positive int durationMinutes,
    @Positive int priceInGems) {}
