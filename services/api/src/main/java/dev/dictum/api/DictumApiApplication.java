package dev.dictum.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(
    info =
        @Info(
            title = "Dictum API",
            description = "Control-plane API for developer-owned personal blogs.",
            version = "0.1.0"))
public class DictumApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(DictumApiApplication.class, args);
  }
}
