package pe.greenminds.ecomind.bdd.iam;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.greenminds.ecomind.bdd.ApiClient;

/**
 * Steps of HU-057, sign in.
 */
public class SignInSteps {

  private final IamApiDriver iam;
  private final ApiClient api;

  public SignInSteps(IamApiDriver iam, ApiClient api) {
    this.iam = iam;
    this.api = api;
  }

  @When("I sign in with email {string} and password {string}")
  public void iSignIn(String email, String password) {
    iam.signIn(email, password);
  }

  @Then("I receive an access token")
  public void iReceiveAnAccessToken() {
    assertThat(api.jsonValue("$.accessToken")).isNotBlank();
    assertThat(api.jsonValue("$.expiresAt")).isNotBlank();
  }

  @Then("I do not receive an access token")
  public void iDoNotReceiveAnAccessToken() {
    assertThat(api.jsonValue("$.accessToken")).isNull();
  }

  @Then("the access token identifies the account {string}")
  public void theAccessTokenIdentifiesTheAccount(String email) {
    iam.getCurrentAccount(true);
    assertThat(api.status()).isEqualTo(200);
    assertThat(api.jsonValue("$.email")).isEqualTo(email);
    assertThat(api.jsonValue("$.accountId")).isNotBlank();
  }
}
