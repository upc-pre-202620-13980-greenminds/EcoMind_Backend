package pe.greenminds.ecomind.shared.infrastructure.i18n.configuration;

import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Resolves the locale of each request from the Accept-Language header.
 * English (en_US) is the default; Latin American Spanish (es_419) is also supported.
 */
@Configuration
public class LocaleConfiguration {

  public static final Locale DEFAULT_LOCALE = Locale.US;
  public static final Locale LATIN_AMERICAN_SPANISH = Locale.forLanguageTag("es-419");

  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(DEFAULT_LOCALE);
    resolver.setSupportedLocales(List.of(DEFAULT_LOCALE, LATIN_AMERICAN_SPANISH));
    return resolver;
  }
}
