package pe.greenminds.ecomind.iam.interfaces.rest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PasswordRecoveryPageControllerTests {
  private MockMvc mvc;

  @BeforeEach
  void setUp() throws Exception {
    var messages = new ResourceBundleMessageSource();
    messages.setBasename("messages");
    messages.setDefaultEncoding("UTF-8");
    messages.setFallbackToSystemLocale(false);
    mvc = MockMvcBuilders.standaloneSetup(new PasswordRecoveryPageController(messages)).build();
  }

  @Test
  void validLinkOffersOnlyTheAndroidHandoffAndDoesNotCacheOrSendReferrers() throws Exception {
    String token = "a".repeat(43);
    mvc.perform(get("/authentication/reset-password").param("token", token)
            .param("redirect", "https://example.invalid").header("Accept-Language", "en"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(header().string("Referrer-Policy", "no-referrer"))
        .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
        .andExpect(content().string(containsString("href=\"ecomind://authentication/reset-password?token=" + token + "\"")))
        .andExpect(content().string(containsString("Open EcoMind")))
        .andExpect(content().string(not(containsString("https://example.invalid"))))
        .andExpect(content().string(not(containsString("<script"))));
  }

  @Test
  void spanishContentRetainsItsEncoding() throws Exception {
    mvc.perform(get("/authentication/reset-password").param("token", "b".repeat(43))
            .header("Accept-Language", "es-419"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Restablece tu contraseña")))
        .andExpect(content().string(containsString("Abrir EcoMind")));
  }

  @Test
  void missingAndInjectedTokensAreRejectedWithoutReflection() throws Exception {
    mvc.perform(get("/authentication/reset-password"))
        .andExpect(status().isBadRequest()).andExpect(header().string("Cache-Control", "no-store"));
    mvc.perform(get("/authentication/reset-password").param("token", "\"><script>alert(1)</script>"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(not(containsString("<script"))))
        .andExpect(content().string(not(containsString("href="))));
  }
}
