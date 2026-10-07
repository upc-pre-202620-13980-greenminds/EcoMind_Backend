package pe.greenminds.ecomind.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

/**
 * Shares the Spring application context with the Cucumber step definitions.
 * Scenarios call the API through MockMvc against the in-memory database of the test profile.
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
