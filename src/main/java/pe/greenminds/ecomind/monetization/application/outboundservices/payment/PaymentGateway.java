package pe.greenminds.ecomind.monetization.application.outboundservices.payment;

import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;

public interface PaymentGateway {
  boolean supports(PaymentMethodType method);
  ChargeResult charge(ChargeRequest request);
}
