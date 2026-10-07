package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import pe.greenminds.ecomind.bdd.ApiClient;

/**
 * Steps of HU-059, log out.
 */
public class LogoutSteps {

  private final IamApiDriver iam;
  private final ApiClient api;

  public LogoutSteps(IamApiDriver iam, ApiClient api) {
    this.iam = iam;
    this.api = api;
  }

  @Given("I am signed in as {string} with password {string}")
  public void iAmSignedIn(String email, String password) {
    iam.signIn(email, password);
    assertThat(api.status()).isEqualTo(200);
  }

  @When("I log out")
  public void iLogOut() {
    iam.logout(true);
  }

  @When("I log out without an access token")
  public void iLogOutWithoutAnAccessToken() {
    iam.logout(false);
  }

  // After logging out the application has discarded its token, so it can only call without one.
  @When("I request my account without an access token")
  public void iRequestMyAccountWithoutAnAccessToken() {
    iam.getCurrentAccount(false);
  }
}
