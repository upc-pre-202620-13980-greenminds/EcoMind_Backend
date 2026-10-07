package pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation of the EcoMind web services, with one group per bounded context.
 */
@Configuration
public class OpenApiConfiguration {

  private static final String BASE_PACKAGE = "pe.greenminds.ecomind.";

  @Bean
  public OpenAPI ecomindOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("EcoMind API")
            .description("RESTful web services of the EcoMind platform.")
            .version("v1"));
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
