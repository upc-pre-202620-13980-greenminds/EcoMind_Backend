package pe.greenminds.ecomind.shared.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedEndpointsIntegrationTests {
  @Autowired MockMvc mvc;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/v1/authentication/me",
        "/api/v1/user",
        "/api/v1/quests",
        "/api/v1/Community/Communities",
        "/api/v1/gamification/me/progress",
        "/api/v1/monetization/me/wallet",
        "/api/v1/monetization/store"
      })
  void unauthenticatedRequestsCannotReadPrivateResources(String path) throws Exception {
    mvc.perform(get(path)).andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/v1/authentication/me",
        "/api/v1/user",
        "/api/v1/quests",
        "/api/v1/Community/Communities",
        "/api/v1/gamification/me/progress",
        "/api/v1/monetization/me/wallet"
      })
  void invalidTokensCannotReadPrivateResources(String path) throws Exception {
    mvc.perform(get(path).header("Authorization", "Bearer invalid.token.value"))
        .andExpect(status().isUnauthorized());
  }
}
