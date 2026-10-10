package pe.greenminds.ecomind.shared.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTests {

  private static final String UNKNOWN_PATH = "/api/v1/unknown-endpoint";

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser
  void unknownEndpointReturnsErrorInEnglishByDefault() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("The requested endpoint does not exist."));
  }

  @Test
  @WithMockUser
  void unknownEndpointReturnsErrorInSpanishWhenRequested() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, "es-419"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("El endpoint solicitado no existe."));
  }

  @Test
  @WithMockUser
  void unsupportedLanguageFallsBackToEnglish() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, "fr-FR"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("The requested endpoint does not exist."));
  }

  @Test
  void requestWithoutTokenIsRejectedInEnglishByDefault() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(
            jsonPath("$.message").value("Authentication is required to access this resource."));
  }

  @Test
  void requestWithoutTokenIsRejectedInSpanishWhenRequested() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.ACCEPT_LANGUAGE, "es-419"))
        .andExpect(status().isUnauthorized())
        .andExpect(
            jsonPath("$.message").value("Debes iniciar sesión para acceder a este recurso."));
  }

  @Test
  void requestWithInvalidTokenIsRejected() throws Exception {
    mockMvc.perform(get(UNKNOWN_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }
}
