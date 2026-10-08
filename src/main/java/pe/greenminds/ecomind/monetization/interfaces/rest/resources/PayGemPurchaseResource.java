package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PayGemPurchaseResource(
    @NotBlank String sourceToken,
    @NotBlank @Email String email) {}
