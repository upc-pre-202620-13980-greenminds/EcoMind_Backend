package pe.greenminds.ecomind.monetization.infrastructure.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.greenminds.ecomind.monetization.application.outboundservices.payment.*;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;

@Component
@ConditionalOnExpression("!'${PAYPAL_CLIENT_ID:}'.isBlank() && !'${PAYPAL_SECRET:}'.isBlank()")
public class PayPalPaymentGateway implements PaymentGateway {
  private final RestClient client;
  private final String clientId;
  private final String secret;

  public PayPalPaymentGateway(
      @Value("${paypal.api.base-url:https://api-m.sandbox.paypal.com}") String baseUrl,
      @Value("${PAYPAL_CLIENT_ID:}") String clientId,
      @Value("${PAYPAL_SECRET:}") String secret) {
    this.client = RestClient.builder().baseUrl(baseUrl).build();
    this.clientId = clientId;
    this.secret = secret;
  }

  @Override public boolean supports(PaymentMethodType method) {
    return method == PaymentMethodType.PAYPAL;
  }

  @Override
  public ChargeResult charge(ChargeRequest request) {
    try {
      String accessToken = accessToken();
      @SuppressWarnings("unchecked")
      Map<String, Object> response = client.post()
          .uri("/v2/checkout/orders/{orderId}/capture", request.sourceToken())
          .header("Authorization", "Bearer " + accessToken)
          .contentType(MediaType.APPLICATION_JSON).body("{}").retrieve().body(Map.class);
      if (response != null && "COMPLETED".equals(String.valueOf(response.get("status")))) {
        return ChargeResult.approved(String.valueOf(response.get("id")));
      }
      return ChargeResult.declined("PayPal order was not completed");
    } catch (Exception exception) {
      return ChargeResult.declined("PayPal is temporarily unavailable");
    }
  }

  private String accessToken() {
    String basic = Base64.getEncoder().encodeToString(
        (clientId + ":" + secret).getBytes(StandardCharsets.UTF_8));
    @SuppressWarnings("unchecked")
    Map<String, Object> response = client.post().uri("/v1/oauth2/token")
        .header("Authorization", "Basic " + basic)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body("grant_type=client_credentials").retrieve().body(Map.class);
    if (response == null || response.get("access_token") == null) {
      throw new IllegalStateException("PayPal returned no access token");
    }
    return response.get("access_token").toString();
  }
}
