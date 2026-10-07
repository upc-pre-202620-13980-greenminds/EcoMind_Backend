package pe.greenminds.ecomind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EcomindApplication {

  public static void main(String[] args) {
    SpringApplication.run(EcomindApplication.class, args);
  }
}
