package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;

public record CreateGemPurchaseResource(
    @NotNull UUID packageId,
    @NotNull PaymentMethodType paymentMethod,
    @NotNull UUID requestId) {}
