package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;

/**
 * Steps of HU-058, password recovery.
 */
public class PasswordRecoverySteps {

  private final IamApiDriver iam;
  private final RecordingEmailService emailService;

  private String recoveryEmail;

  public PasswordRecoverySteps(IamApiDriver iam, RecordingEmailService emailService) {
    this.iam = iam;
    this.emailService = emailService;
  }

  @When("I request a password recovery for {string}")
  @Given("I requested a password recovery for {string}")
  public void iRequestAPasswordRecovery(String email) {
    recoveryEmail = email;
    iam.requestPasswordRecovery(email);
  }

  @When("I set the new password {string} with the recovery link I received")
  public void iSetTheNewPassword(String newPassword) {
    String token = emailService.recoveryTokenFor(recoveryEmail).orElseThrow();
    iam.confirmPasswordRecovery(token, newPassword);
  }

  @Then("a recovery link is sent to {string}")
  public void aRecoveryLinkIsSentTo(String email) {
    assertThat(emailService.recoveryTokenFor(email)).isPresent();
  }

  @Then("no recovery link is sent")
  public void noRecoveryLinkIsSent() {
    assertThat(emailService.recoveryEmailsSent()).isZero();
  }
}
