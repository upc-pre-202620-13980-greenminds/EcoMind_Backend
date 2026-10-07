package pe.greenminds.ecomind.iam.support;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Email adapter for tests: keeps what would have been sent so the tests can read it.
 */
@Component
@Profile("test")
public class RecordingEmailService implements EmailService {

  private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
  private final Map<String, String> recoveryTokens = new ConcurrentHashMap<>();

  @Override
  public void sendVerificationCode(EmailAddress recipient, String name, String verificationCode) {
    verificationCodes.put(recipient.value(), verificationCode);
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken) {
    recoveryTokens.put(recipient.value(), recoveryToken);
  }

  public Optional<String> verificationCodeFor(String email) {
    return Optional.ofNullable(verificationCodes.get(email));
  }

  public Optional<String> recoveryTokenFor(String email) {
    return Optional.ofNullable(recoveryTokens.get(email));
  }

  public int recoveryEmailsSent() {
    return recoveryTokens.size();
  }

  public void reset() {
    verificationCodes.clear();
    recoveryTokens.clear();
  }
}
