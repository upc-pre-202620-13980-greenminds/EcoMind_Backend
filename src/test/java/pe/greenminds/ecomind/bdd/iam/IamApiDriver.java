package pe.greenminds.ecomind.bdd.iam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.cucumber.spring.ScenarioScope;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.greenminds.ecomind.iam.support.RecordingEmailService;

/**
 * Calls the IAM endpoints for the step definitions and remembers the last response and the
 * access token of the scenario.
 */
@Component
@ScenarioScope
public class IamApiDriver {

  private static final String BASE_PATH = "/api/v1/authentication";

  private final MockMvc mockMvc;
  private final RecordingEmailService emailService;

  private MvcResult lastResult;
  private String language;
  private String accessToken;

  public IamApiDriver(MockMvc mockMvc, RecordingEmailService emailService) {
    this.mockMvc = mockMvc;
    this.emailService = emailService;
  }

  public void useLanguage(String languageTag) {
    this.language = languageTag;
  }

  public void signUp(String name, String email, String password, String socialRole) {
    postJson(
        "/sign-up",
        Map.of("name", name, "email", email, "password", password, "socialRole", socialRole),
        false);
  }

  public void verifyEmail(String email, String code) {
    postJson("/verify-email", Map.of("email", email, "code", code), false);
  }

  /** Runs the whole registration so the scenario starts with an existing account. */
  public void registerAccount(String email, String password) {
    signUp("Test User", email, password, "STUDENT");
    verifyEmail(email, emailService.verificationCodeFor(email).orElseThrow());
    if (status() != 201) {
      throw new IllegalStateException("The account could not be registered: " + body());
    }
  }

  public void signIn(String email, String password) {
    postJson("/sign-in", Map.of("email", email, "password", password), false);
    accessToken = status() == 200 ? jsonValue("$.accessToken") : null;
  }

  public void requestPasswordRecovery(String email) {
    postJson("/password-recovery/request", Map.of("email", email), false);
  }

  public void confirmPasswordRecovery(String token, String newPassword) {
    postJson(
        "/password-recovery/confirm", Map.of("token", token, "newPassword", newPassword), false);
  }

  public void logout(boolean withAccessToken) {
    perform(post(BASE_PATH + "/logout"), withAccessToken);
  }

  public void getCurrentAccount(boolean withAccessToken) {
    perform(get(BASE_PATH + "/me"), withAccessToken);
  }

  public int status() {
    return lastResult.getResponse().getStatus();
  }

  public String body() {
    try {
      return lastResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  /** Value of a JSON path in the last response, or null when the path does not exist. */
  public String jsonValue(String path) {
    try {
      Object value = JsonPath.read(body(), path);
      return value == null ? null : value.toString();
    } catch (PathNotFoundException ex) {
      return null;
    }
  }

  private void postJson(String path, Map<String, String> fields, boolean withAccessToken) {
    perform(
        post(BASE_PATH + path).contentType(MediaType.APPLICATION_JSON).content(toJson(fields)),
        withAccessToken);
  }

  private void perform(MockHttpServletRequestBuilder request, boolean withAccessToken) {
    if (language != null) {
      request.header(HttpHeaders.ACCEPT_LANGUAGE, language);
    }
    if (withAccessToken && accessToken != null) {
      request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }
    try {
      lastResult = mockMvc.perform(request).andReturn();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static String toJson(Map<String, String> fields) {
    return fields.entrySet().stream()
        .map(entry -> "\"%s\":\"%s\"".formatted(entry.getKey(), escape(entry.getValue())))
        .collect(Collectors.joining(",", "{", "}"));
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
