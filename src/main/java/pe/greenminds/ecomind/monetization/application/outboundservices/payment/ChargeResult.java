package pe.greenminds.ecomind.monetization.application.outboundservices.payment;

public record ChargeResult(boolean approved, String reference, String message) {
  public static ChargeResult approved(String reference) {
    return new ChargeResult(true, reference, "Charge approved");
  }

  public static ChargeResult declined(String message) {
    return new ChargeResult(false, null, message);
  }
}
