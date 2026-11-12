package pe.greenminds.ecomind.iam.infrastructure.email.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.iam.application.outboundservices.EmailService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Development replacement for the email provider: writes the code or token to the console so the
 * flows can be tried without sending real emails. It is selected for dev unless real email is explicitly enabled.
 */
@Service
@Profile("dev & !email")
public class ConsoleEmailService implements EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleEmailService.class);

  @Override
  public void sendVerificationCode(EmailAddress recipient, String name, String verificationCode) {
    LOGGER.info("Verification code for {}: {}", recipient.value(), verificationCode);
  }

  @Override
  public void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken) {
    LOGGER.info("Password recovery token for {}: {}", recipient.value(), recoveryToken);
  }
}
