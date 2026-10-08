package pe.greenminds.ecomind.monetization.infrastructure.payment;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.greenminds.ecomind.monetization.application.outboundservices.payment.*;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType;

@Component
@ConditionalOnExpression("!'${CULQI_SECRET_KEY:}'.isBlank()")
public class CulqiPaymentGateway implements PaymentGateway {
  private final RestClient client;
  private final String secretKey;

  public CulqiPaymentGateway(
      @Value("${culqi.api.base-url:https://api.culqi.com/v2}") String baseUrl,
      @Value("${CULQI_SECRET_KEY:}") String secretKey) {
    this.client = RestClient.builder().baseUrl(baseUrl).build();
    this.secretKey = secretKey;
  }

  @Override
  public boolean supports(PaymentMethodType method) {
    return method == PaymentMethodType.CARD || method == PaymentMethodType.YAPE;
  }

  @Override
  public ChargeResult charge(ChargeRequest request) {
    var body = new LinkedHashMap<String, Object>();
    body.put("amount", request.amountInCents());
    body.put("currency_code", request.currency());
    body.put("email", request.email());
    body.put("source_id", request.sourceToken());
    body.put("description", request.description());
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = client.post().uri("/charges")
          .header("Authorization", "Bearer " + secretKey)
          .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
      Object id = response == null ? null : response.get("id");
      return id == null ? ChargeResult.declined("Culqi returned no charge reference")
          : ChargeResult.approved(id.toString());
    } catch (RestClientResponseException exception) {
      return ChargeResult.declined("Culqi declined the charge");
    } catch (Exception exception) {
      return ChargeResult.declined("Culqi is temporarily unavailable");
    }
  }
}
