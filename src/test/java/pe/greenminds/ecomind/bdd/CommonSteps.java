package pe.greenminds.ecomind.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;

/**
 * Steps shared by every feature: clean state before each scenario and assertions on the last
 * response.
 */
public class CommonSteps {

  // Child tables first, so no foreign key is violated.
  private static final List<String> TABLES =
      List.of(
          "friendships",
          "family_members",
          "families",
          "user_profiles",
          "email_verifications",
          "pending_registrations",
          "password_reset_tokens",
          "account_credentials",
          "accounts");

  private final ApiClient api;
  private final JdbcTemplate jdbcTemplate;
  private final RecordingEmailService emailService;

  public CommonSteps(
      ApiClient api, JdbcTemplate jdbcTemplate, RecordingEmailService emailService) {
    this.api = api;
    this.jdbcTemplate = jdbcTemplate;
    this.emailService = emailService;
  }

  @Before
  public void cleanState() {
    TABLES.forEach(table -> jdbcTemplate.update("DELETE FROM " + table));
    emailService.reset();
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
