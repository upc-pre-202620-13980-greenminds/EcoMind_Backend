package pe.greenminds.ecomind.monetization.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreControllerTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private TokenService tokens;

  @Test
  void catalogRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/monetization/store"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatedUserCanBrowseTheCatalog() throws Exception {
    mockMvc.perform(get("/api/v1/monetization/store")
            .header(HttpHeaders.AUTHORIZATION, bearer(501L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cosmetics").isArray())
        .andExpect(jsonPath("$.multipliers").isArray())
        .andExpect(jsonPath("$.streakProtectors").isArray())
        .andExpect(jsonPath("$.gemPackages").isArray());
  }

  private String bearer(long accountId) {
    var user = new AuthenticatedUser(
        new AccountId(accountId), new EmailAddress(accountId + "@example.com"));
    return "Bearer " + tokens.issueAccessToken(user).value();
  }
}
