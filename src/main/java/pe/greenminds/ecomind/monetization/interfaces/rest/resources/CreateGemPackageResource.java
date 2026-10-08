package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateGemPackageResource(
    @NotBlank @Size(max = 120) String name,
    @Positive int gemAmount,
    @NotNull @Positive BigDecimal price,
    @NotBlank @Pattern(regexp = "(?i)[A-Z]{3}") String currency) {}
