package pe.greenminds.ecomind.bdd;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Sends requests to the API through MockMvc and keeps the last response of the scenario so any
 * step definition can assert on it.
 */
@Component
@ScenarioScope
public class ApiClient {

  private final MockMvc mockMvc;

  private MvcResult lastResult;
  private String language;

  public ApiClient(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  public void useLanguage(String languageTag) {
    this.language = languageTag;
  }

  /** Sends a POST with a JSON body. The access token is optional. */
  public void post(String path, Map<String, Object> body, String accessToken) {
    perform(withJson(MockMvcRequestBuilders.post(path), body), accessToken);
  }

  public void put(String path, Map<String, Object> body, String accessToken) {
    perform(withJson(MockMvcRequestBuilders.put(path), body), accessToken);
  }

  public void patch(String path, Map<String, Object> body, String accessToken) {
    perform(withJson(MockMvcRequestBuilders.patch(path), body), accessToken);
  }

  public void patchWithoutBody(String path, String accessToken) {
    perform(MockMvcRequestBuilders.patch(path), accessToken);
  }

  public void postWithoutBody(String path, String accessToken) {
    perform(MockMvcRequestBuilders.post(path), accessToken);
  }

  public void get(String path, String accessToken) {
    perform(MockMvcRequestBuilders.get(path), accessToken);
  }

  public void delete(String path, String accessToken) {
    perform(MockMvcRequestBuilders.delete(path), accessToken);
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
    Object value = jsonObject(path);
    return value == null ? null : value.toString();
  }

  /** Raw value of a JSON path (a list for array paths), or null when the path does not exist. */
  public <T> T jsonObject(String path) {
    try {
      return JsonPath.read(body(), path);
    } catch (PathNotFoundException ex) {
      return null;
    }
  }

  private void perform(MockHttpServletRequestBuilder request, String accessToken) {
    if (language != null) {
      request.header(HttpHeaders.ACCEPT_LANGUAGE, language);
    }
    if (accessToken != null) {
      request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }
    try {
      lastResult = mockMvc.perform(request).andReturn();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static MockHttpServletRequestBuilder withJson(
      MockHttpServletRequestBuilder request, Map<String, Object> body) {
    return request.contentType(MediaType.APPLICATION_JSON).content(toJson(body));
  }

  private static String toJson(Map<String, Object> fields) {
    return fields.entrySet().stream()
        .map(entry -> "\"%s\":%s".formatted(entry.getKey(), toJsonValue(entry.getValue())))
        .collect(Collectors.joining(",", "{", "}"));
  }

  private static String toJsonValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      return map.entrySet().stream()
          .map(entry -> toJsonValue(entry.getKey().toString()) + ":" + toJsonValue(entry.getValue()))
          .collect(Collectors.joining(",", "{", "}"));
    }
    if (value instanceof String text) {
      return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    return String.valueOf(value);
  }
}
