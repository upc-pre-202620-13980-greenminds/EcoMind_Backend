package pe.greenminds.ecomind.monetization.application.outboundservices.payment;

public record ChargeRequest(
    int amountInCents, String currency, String email, String sourceToken, String description) {}
