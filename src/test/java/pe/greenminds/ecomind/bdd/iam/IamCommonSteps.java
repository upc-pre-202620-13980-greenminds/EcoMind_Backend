package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;
import pe.greenminds.ecomind.iam.support.RecordingUsersContextGateway;

/**
 * Steps shared by the IAM features: scenario setup and assertions on the last response.
 */
public class IamCommonSteps {

  private final IamApiDriver api;
  private final JdbcTemplate jdbcTemplate;
  private final RecordingEmailService emailService;
  private final RecordingUsersContextGateway usersContextGateway;

  public IamCommonSteps(
      IamApiDriver api,
      JdbcTemplate jdbcTemplate,
      RecordingEmailService emailService,
      RecordingUsersContextGateway usersContextGateway) {
    this.api = api;
    this.jdbcTemplate = jdbcTemplate;
    this.emailService = emailService;
    this.usersContextGateway = usersContextGateway;
  }

  @Before
  public void cleanState() {
    jdbcTemplate.update("DELETE FROM email_verifications");
    jdbcTemplate.update("DELETE FROM pending_registrations");
    jdbcTemplate.update("DELETE FROM password_reset_tokens");
    jdbcTemplate.update("DELETE FROM account_credentials");
    jdbcTemplate.update("DELETE FROM accounts");
    emailService.reset();
    usersContextGateway.reset();
  }

  @Given("a registered account with email {string} and password {string}")
  public void aRegisteredAccount(String email, String password) {
    api.registerAccount(email, password);
    emailService.reset();
    usersContextGateway.reset();
  }

  @Given("my language is {string}")
  public void myLanguageIs(String languageTag) {
    api.useLanguage(languageTag);
  }

  @Then("the response status is {int}")
  public void theResponseStatusIs(int status) {
    assertThat(api.status()).as(api.body()).isEqualTo(status);
  }

  @Then("the error code is {string}")
  public void theErrorCodeIs(String code) {
    assertThat(api.jsonValue("$.code")).isEqualTo(code);
  }

  @Then("the error message is {string}")
  public void theErrorMessageIs(String message) {
    assertThat(api.jsonValue("$.message")).isEqualTo(message);
  }

  @Then("the response message is {string}")
  public void theResponseMessageIs(String message) {
    assertThat(api.jsonValue("$.message")).isEqualTo(message);
  }

  @Then("the error details mention {string}")
  public void theErrorDetailsMention(String text) {
    assertThat(api.jsonValue("$.details")).contains(text);
  }
}
