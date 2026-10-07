package pe.greenminds.ecomind.shared.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTests {

  private static final String UNKNOWN_PATH = "/api/v1/unknown-endpoint";

  @Autowired
  private MockMvc mockMvc;

  @Test
  void unknownEndpointReturnsErrorInEnglishByDefault() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("The requested endpoint does not exist."));
  }

  @Test
  void unknownEndpointReturnsErrorInSpanishWhenRequested() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, "es-419"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("El endpoint solicitado no existe."));
  }

  @Test
  void unsupportedLanguageFallsBackToEnglish() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, "fr-FR"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("The requested endpoint does not exist."));
  }
}
