package pe.greenminds.ecomind.support;

import java.sql.Timestamp;
import java.time.Instant;

/** PostgreSQL wall-clock function used by repository queries in the H2 test profile. */
public final class H2Clock {
  private H2Clock() {}

  public static Timestamp timestamp() {
    return Timestamp.from(Instant.now());
  }
}
