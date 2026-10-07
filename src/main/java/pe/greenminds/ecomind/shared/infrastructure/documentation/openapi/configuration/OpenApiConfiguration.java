package pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation of the EcoMind web services, with one group per bounded context.
 */
@Configuration
public class OpenApiConfiguration {

  /** Name used by protected endpoints in their security requirement. */
  public static final String BEARER_AUTH_SCHEME = "bearerAuth";

  private static final String BASE_PACKAGE = "pe.greenminds.ecomind.";

  @Bean
  public OpenAPI ecomindOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("EcoMind API")
            .description("RESTful web services of the EcoMind platform.")
            .version("v1"))
        .components(new Components()
            .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access token returned by the sign-in endpoint.")));
  }

  @Bean
  public GroupedOpenApi iamApi() {
    return groupFor("iam");
  }

  @Bean
  public GroupedOpenApi usersApi() {
    return groupFor("users");
  }

  @Bean
  public GroupedOpenApi learningApi() {
    return groupFor("learning");
  }

  @Bean
  public GroupedOpenApi questsApi() {
    return groupFor("quests");
  }

  @Bean
  public GroupedOpenApi communityApi() {
    return groupFor("community");
  }

  @Bean
  public GroupedOpenApi gamificationApi() {
    return groupFor("gamification");
  }

  @Bean
  public GroupedOpenApi monetizationApi() {
    return groupFor("monetization");
  }

  private static GroupedOpenApi groupFor(String context) {
    return GroupedOpenApi.builder()
        .group(context)
        .packagesToScan(BASE_PACKAGE + context)
        .build();
  }
}
