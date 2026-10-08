package pe.greenminds.ecomind.monetization.application.outboundservices.payment;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;

@Service
public class PaymentGatewayResolver {
  private final List<PaymentGateway> gateways;

  public PaymentGatewayResolver(List<PaymentGateway> gateways) {
    this.gateways = gateways;
  }

  public PaymentGateway require(PaymentMethodType method) {
    return gateways.stream().filter(gateway -> gateway.supports(method)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Payment gateway is not configured for " + method));
  }
}
