package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;
import pe.greenminds.ecomind.iam.support.RecordingUsersContextGateway;

/**
 * Steps of HU-056, user registration.
 */
public class UserRegistrationSteps {

  private final IamApiDriver api;
  private final JdbcTemplate jdbcTemplate;
  private final RecordingEmailService emailService;
  private final RecordingUsersContextGateway usersContextGateway;

  public UserRegistrationSteps(
      IamApiDriver api,
      JdbcTemplate jdbcTemplate,
      RecordingEmailService emailService,
      RecordingUsersContextGateway usersContextGateway) {
    this.api = api;
    this.jdbcTemplate = jdbcTemplate;
    this.emailService = emailService;
    this.usersContextGateway = usersContextGateway;
  }

  @Given("no account exists for {string}")
  public void noAccountExistsFor(String email) {
    assertThat(accountsWithEmail(email)).isZero();
  }

  @When("I sign up with name {string}, email {string}, password {string} and role {string}")
  public void iSignUp(String name, String email, String password, String role) {
    api.signUp(name, email, password, role);
  }

  @When("I verify {string} with the code I received")
  public void iVerifyWithTheCodeIReceived(String email) {
    api.verifyEmail(email, emailService.verificationCodeFor(email).orElseThrow());
  }

  @When("I verify {string} with a wrong code")
  public void iVerifyWithAWrongCode(String email) {
    String realCode = emailService.verificationCodeFor(email).orElseThrow();
    api.verifyEmail(email, realCode.equals("000000") ? "111111" : "000000");
  }

  @Then("a verification code is sent to {string}")
  public void aVerificationCodeIsSentTo(String email) {
    assertThat(emailService.verificationCodeFor(email)).isPresent();
  }

  @Then("no verification code is sent to {string}")
  public void noVerificationCodeIsSentTo(String email) {
    assertThat(emailService.verificationCodeFor(email)).isEmpty();
  }

  @Then("the account {string} exists")
  public void theAccountExists(String email) {
    assertThat(accountsWithEmail(email)).isEqualTo(1);
  }

  @Then("the account {string} does not exist")
  public void theAccountDoesNotExist(String email) {
    assertThat(accountsWithEmail(email)).isZero();
  }

  @Then("a profile is requested for {string} with role {string}")
  public void aProfileIsRequested(String name, String role) {
    assertThat(usersContextGateway.requestedProfiles())
        .singleElement()
        .satisfies(
            profile -> {
              assertThat(profile.name()).isEqualTo(name);
              assertThat(profile.socialRole()).isEqualTo(SocialRole.valueOf(role));
              assertThat(profile.accountId()).isNotNull();
            });
  }

  private int accountsWithEmail(String email) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM account_credentials WHERE email = ?", Integer.class, email);
    return count == null ? 0 : count;
  }
}
