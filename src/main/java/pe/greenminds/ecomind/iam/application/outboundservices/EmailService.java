package pe.greenminds.ecomind.iam.application.outboundservices;

import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

/**
 * Outbound port for the emails IAM needs to send.
 */
public interface EmailService {

  void sendVerificationCode(EmailAddress recipient, String name, String verificationCode);

  void sendPasswordRecoveryLink(EmailAddress recipient, String recoveryToken);
}
