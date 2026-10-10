package pe.greenminds.ecomind;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
class EcomindApplicationTests {

  @Test
  void contextLoads() {
  }
}
