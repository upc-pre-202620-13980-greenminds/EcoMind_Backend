package pe.greenminds.ecomind.monetization.interfaces.rest;

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
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GemWalletControllerTests {
  @Autowired private MockMvc mockMvc;
  @Autowired private TokenService tokens;
  @Autowired private UserProfileRepository profiles;

  @Test
  void walletRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/monetization/me/wallet"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void inventoryAndPurchasesRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/monetization/me/inventory"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticatedUserStartsWithAnEmptyWalletAndHistory() throws Exception {
    var id = new UserId(987654L);
    if (!profiles.existsById(id))
      profiles.save(UserProfile.create(id, "Wallet Test", SocialRole.STUDENT));
    var authorization = bearer(987654L);
    mockMvc.perform(get("/api/v1/monetization/me/wallet")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(0));
    mockMvc.perform(get("/api/v1/monetization/me/wallet/movements")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  private String bearer(long accountId) {
    var user = new AuthenticatedUser(
        new AccountId(accountId), new EmailAddress(accountId + "@example.com"));
    return "Bearer " + tokens.issueAccessToken(user).value();
  }
}
