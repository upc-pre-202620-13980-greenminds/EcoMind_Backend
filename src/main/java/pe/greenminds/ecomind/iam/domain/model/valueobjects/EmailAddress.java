package pe.greenminds.ecomind.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Email address normalized to lowercase without surrounding spaces.
 */
public record EmailAddress(String value) {

  private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final int MAX_LENGTH = 255;

  public EmailAddress {
    if (value == null) {
      throw new IllegalArgumentException("Email address is required");
    }
    value = value.trim().toLowerCase(Locale.ROOT);
    if (value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("Email address is not valid");
    }
  }
}
