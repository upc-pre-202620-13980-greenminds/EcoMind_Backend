package pe.greenminds.ecomind.bdd.iam;

import io.cucumber.spring.ScenarioScope;
import java.util.Map;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;

/**
 * Calls the IAM endpoints for the step definitions and remembers the access token of the
 * scenario.
 */
@Component
@ScenarioScope
public class IamApiDriver {

  private static final String BASE_PATH = "/api/v1/authentication";

  private final ApiClient api;
  private final RecordingEmailService emailService;

  private String accessToken;

  public IamApiDriver(ApiClient api, RecordingEmailService emailService) {
    this.api = api;
    this.emailService = emailService;
  }

  public void signUp(String name, String email, String password, String socialRole) {
    api.post(
        BASE_PATH + "/sign-up",
        Map.of("name", name, "email", email, "password", password, "socialRole", socialRole),
        null);
  }

  public void verifyEmail(String email, String code) {
    api.post(BASE_PATH + "/verify-email", Map.of("email", email, "code", code), null);
  }

  /** Runs the whole registration and returns the id of the new account. */
  public Long registerAccount(String name, String email, String password, String socialRole) {
    signUp(name, email, password, socialRole);
    verifyEmail(email, emailService.verificationCodeFor(email).orElseThrow());
    if (api.status() != 201) {
      throw new IllegalStateException("The account could not be registered: " + api.body());
    }
    return Long.valueOf(api.jsonValue("$.accountId"));
  }

  /** Signs in and returns the access token, or null when the credentials are rejected. */
  public String signIn(String email, String password) {
    api.post(BASE_PATH + "/sign-in", Map.of("email", email, "password", password), null);
    accessToken = api.status() == 200 ? api.jsonValue("$.accessToken") : null;
    return accessToken;
  }

  public void requestPasswordRecovery(String email) {
    api.post(BASE_PATH + "/password-recovery/request", Map.of("email", email), null);
  }

  public void confirmPasswordRecovery(String token, String newPassword) {
    api.post(
        BASE_PATH + "/password-recovery/confirm",
        Map.of("token", token, "newPassword", newPassword),
        null);
  }

  public void logout(boolean withAccessToken) {
    api.postWithoutBody(BASE_PATH + "/logout", withAccessToken ? accessToken : null);
  }

  public void getCurrentAccount(boolean withAccessToken) {
    api.get(BASE_PATH + "/me", withAccessToken ? accessToken : null);
  }
}
