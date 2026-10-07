package pe.greenminds.ecomind.shared.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless security for every bounded context: public routes are listed here and any other
 * request must carry a valid bearer token.
 */
@Configuration
public class WebSecurityConfiguration {

  private static final String[] PUBLIC_AUTHENTICATION_ROUTES = {
      "/api/v1/authentication/sign-up",
      "/api/v1/authentication/verify-email",
      "/api/v1/authentication/sign-in",
      "/api/v1/authentication/password-recovery/**"
  };

  private static final String[] DOCUMENTATION_ROUTES = {
      "/v3/api-docs/**",
      "/swagger-ui.html",
      "/swagger-ui/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AccessTokenVerifier accessTokenVerifier,
      UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandlerEntryPoint)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(unauthorizedRequestHandlerEntryPoint))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, PUBLIC_AUTHENTICATION_ROUTES).permitAll()
                    .requestMatchers(DOCUMENTATION_ROUTES).permitAll()
                    .anyRequest().authenticated())
        .addFilterBefore(
            new BearerAuthorizationRequestFilter(accessTokenVerifier),
            UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
