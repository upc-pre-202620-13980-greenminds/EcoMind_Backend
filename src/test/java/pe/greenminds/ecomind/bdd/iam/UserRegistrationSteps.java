package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;

/**
 * Steps of HU-056, user registration.
 */
public class UserRegistrationSteps {

  private final IamApiDriver iam;
  private final JdbcTemplate jdbcTemplate;
  private final RecordingEmailService emailService;

  public UserRegistrationSteps(
      IamApiDriver iam, JdbcTemplate jdbcTemplate, RecordingEmailService emailService) {
    this.iam = iam;
    this.jdbcTemplate = jdbcTemplate;
    this.emailService = emailService;
  }

  @Given("no account exists for {string}")
  public void noAccountExistsFor(String email) {
    assertThat(accountsWithEmail(email)).isZero();
  }

  @Given("a registered account with email {string} and password {string}")
  public void aRegisteredAccount(String email, String password) {
    iam.registerAccount("Test User", email, password, "STUDENT");
    emailService.reset();
  }

  @When("I sign up with name {string}, email {string}, password {string} and role {string}")
  public void iSignUp(String name, String email, String password, String role) {
    iam.signUp(name, email, password, role);
  }

  @When("I verify {string} with the code I received")
  public void iVerifyWithTheCodeIReceived(String email) {
    iam.verifyEmail(email, emailService.verificationCodeFor(email).orElseThrow());
  }

  @When("I verify {string} with a wrong code")
  public void iVerifyWithAWrongCode(String email) {
    String realCode = emailService.verificationCodeFor(email).orElseThrow();
    iam.verifyEmail(email, realCode.equals("000000") ? "111111" : "000000");
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

  @Then("the profile of {string} is created with name {string} and role {string}")
  public void theProfileIsCreated(String email, String name, String role) {
    Integer profiles =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM user_profiles p
            JOIN account_credentials c ON c.account_id = p.user_id
            WHERE c.email = ? AND p.name = ? AND p.social_role = ?
            """,
            Integer.class,
            email,
            name,
            role);
    assertThat(profiles).isEqualTo(1);
  }

  private int accountsWithEmail(String email) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM account_credentials WHERE email = ?", Integer.class, email);
    return count == null ? 0 : count;
  }
}
