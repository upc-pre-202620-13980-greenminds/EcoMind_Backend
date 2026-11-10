package pe.greenminds.ecomind.iam.application.outboundservices;

public class EmailDeliveryException extends RuntimeException {
  public EmailDeliveryException(Throwable cause) {
    super("Email delivery to the provider could not be confirmed.", cause);
  }
}
