package pe.greenminds.ecomind.monetization.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.monetization.application.internal.eventhandlers.MonetizationCatalogSeed;
import pe.greenminds.ecomind.monetization.application.outboundservices.payment.*;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MonetizationPurchaseIntegrationTests.PaymentConfiguration.class)
class MonetizationPurchaseIntegrationTests {
  @Autowired private MockMvc mvc;
  @Autowired private TokenService tokens;
  @Autowired private UserProfileRepository profiles;

  @Test
  void repeatedItemPurchaseIsIdempotent() throws Exception {
    long userId = 880001L;
    createUser(userId, 1000);
    String authorization = bearer(userId);
    UUID request = UUID.randomUUID();
    String body = """
        {"itemId":"%s","requestId":"%s"}
        """.formatted(MonetizationCatalogSeed.STREAK_SHIELD, request);

    mvc.perform(post("/api/v1/monetization/me/protectors/purchases")
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(1));
    mvc.perform(post("/api/v1/monetization/me/protectors/purchases")
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(1));
    mvc.perform(get("/api/v1/monetization/me/wallet")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(900));
  }

  @Test
  void repeatedCosmeticAndMultiplierPurchasesDoNotDuplicateItemsOrDebits() throws Exception {
    long userId = 880004L;
    createUser(userId, 1000);
    String authorization = bearer(userId);
    String cosmeticBody = """
        {"itemId":"%s","requestId":"%s"}
        """.formatted(MonetizationCatalogSeed.LEAF_AVATAR, UUID.randomUUID());
    String multiplierBody = """
        {"itemId":"%s","requestId":"%s"}
        """.formatted(MonetizationCatalogSeed.XP_BOOST, UUID.randomUUID());

    for (int attempt = 0; attempt < 2; attempt++) {
      mvc.perform(post("/api/v1/monetization/me/cosmetics/purchases")
              .header(HttpHeaders.AUTHORIZATION, authorization)
              .contentType(MediaType.APPLICATION_JSON).content(cosmeticBody))
          .andExpect(status().isCreated());
      mvc.perform(post("/api/v1/monetization/me/multipliers/purchases")
              .header(HttpHeaders.AUTHORIZATION, authorization)
              .contentType(MediaType.APPLICATION_JSON).content(multiplierBody))
          .andExpect(status().isCreated());
    }
    mvc.perform(get("/api/v1/monetization/me/inventory")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cosmetics.length()").value(1))
        .andExpect(jsonPath("$.multipliers.length()").value(1));
    mvc.perform(get("/api/v1/monetization/me/wallet")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(770));
  }

  @Test
  void inventoryUsesStableApiResources() throws Exception {
    long userId = 880002L;
    createUser(userId, 1000);
    String authorization = bearer(userId);
    String body = """
        {"itemId":"%s","requestId":"%s"}
        """.formatted(MonetizationCatalogSeed.LEAF_AVATAR, UUID.randomUUID());
    mvc.perform(post("/api/v1/monetization/me/cosmetics/purchases")
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.cosmeticId").value(MonetizationCatalogSeed.LEAF_AVATAR.toString()))
        .andExpect(jsonPath("$.userId").doesNotExist());
    mvc.perform(get("/api/v1/monetization/me/inventory")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cosmetics.length()").value(1))
        .andExpect(jsonPath("$.protectors").isArray())
        .andExpect(jsonPath("$.multipliers").isArray());
  }

  @Test
  void approvedGemPackagePaymentCreditsWalletOnlyOnce() throws Exception {
    long userId = 880003L;
    createUser(userId, 0);
    String authorization = bearer(userId);
    String checkoutBody = """
        {"packageId":"%s","paymentMethod":"CARD","requestId":"%s"}
        """.formatted(MonetizationCatalogSeed.STARTER_GEMS, UUID.randomUUID());
    String checkout = mvc.perform(post("/api/v1/monetization/me/gem-purchases")
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .contentType(MediaType.APPLICATION_JSON).content(checkoutBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
        .andReturn().getResponse().getContentAsString();
    String purchaseId = JsonPath.read(checkout, "$.id");
    mvc.perform(get("/api/v1/monetization/me/gem-purchases/{id}", purchaseId)
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(purchaseId))
        .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    String paymentBody = """
        {"sourceToken":"approved-token","email":"buyer@example.com"}
        """;
    for (int attempt = 0; attempt < 2; attempt++) {
      mvc.perform(post("/api/v1/monetization/me/gem-purchases/{id}/payment", purchaseId)
              .header(HttpHeaders.AUTHORIZATION, authorization)
              .contentType(MediaType.APPLICATION_JSON).content(paymentBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.paymentStatus").value("APPROVED"));
    }
    mvc.perform(get("/api/v1/monetization/me/wallet")
            .header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(100));
  }

  @Test
  void catalogProductsCanBeCreatedListedAndRetrievedById() throws Exception {
    long userId = 880005L;
    createUser(userId, 0);
    String authorization = bearer(userId);

    assertCreatedProductCanBeRetrieved(
        authorization,
        "/api/v1/monetization/cosmetics",
        """
            {"name":"Ocean Avatar","description":"Blue ecological avatar",
             "priceInGems":90,"type":"AVATAR","imageUrl":"avatar_ocean"}
            """,
        "Ocean Avatar");
    assertCreatedProductCanBeRetrieved(
        authorization,
        "/api/v1/monetization/multipliers",
        """
            {"name":"Triple XP","description":"Triples XP for fifteen minutes",
             "factor":3.0,"durationMinutes":15,"priceInGems":250}
            """,
        "Triple XP");
    assertCreatedProductCanBeRetrieved(
        authorization,
        "/api/v1/monetization/streak-protectors",
        """
            {"name":"Eco Shield","description":"Protects one missed day",
             "priceInGems":120}
            """,
        "Eco Shield");
    assertCreatedProductCanBeRetrieved(
        authorization,
        "/api/v1/monetization/gem-packages",
        """
            {"name":"Forest Gems","gemAmount":500,"price":9.90,"currency":"PEN"}
            """,
        "Forest Gems");
  }

  private void assertCreatedProductCanBeRetrieved(
      String authorization, String endpoint, String body, String expectedName) throws Exception {
    String response = mvc.perform(post(endpoint)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(expectedName))
        .andReturn().getResponse().getContentAsString();
    String id = JsonPath.read(response, "$.id");

    mvc.perform(get(endpoint).header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(id)).exists());
    mvc.perform(get(endpoint + "/{id}", id).header(HttpHeaders.AUTHORIZATION, authorization))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.name").value(expectedName));
  }

  private void createUser(long id, int gems) {
    var profile = UserProfile.create(new UserId(id), "Store Test", SocialRole.STUDENT);
    profile.updateProgress(0, null, 0, gems);
    profiles.save(profile);
  }

  private String bearer(long id) {
    var user = new AuthenticatedUser(new AccountId(id), new EmailAddress(id + "@example.com"));
    return "Bearer " + tokens.issueAccessToken(user).value();
  }

  @TestConfiguration
  static class PaymentConfiguration {
    @Bean
    PaymentGateway approvedPaymentGateway() {
      return new PaymentGateway() {
        @Override public boolean supports(pe.greenminds.ecomind.monetization.domain.model.valueobjects.PaymentMethodType method) { return true; }
        @Override public ChargeResult charge(ChargeRequest request) {
          return ChargeResult.approved("test-" + UUID.randomUUID());
        }
      };
    }
  }
}
