package pe.greenminds.ecomind.iam.infrastructure.email;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import org.springframework.core.io.ClassPathResource;

/** Bundled logo shared by delivery adapters without remote image requests. */
public final class EmailBranding {
  public static final String LOGO_CONTENT_ID = "ecomind-logo";
  public static final String LOGO_FILENAME = "ecomind-logo.png";
  private static final byte[] LOGO = loadLogo();

  private EmailBranding() {}

  public static byte[] logoBytes() { return LOGO.clone(); }

  public static String logoBase64() { return Base64.getEncoder().encodeToString(LOGO); }

  private static byte[] loadLogo() {
    try {
      return new ClassPathResource("email/" + LOGO_FILENAME).getContentAsByteArray();
    } catch (IOException exception) {
      throw new UncheckedIOException("The EcoMind email logo is unavailable.", exception);
    }
  }
}
