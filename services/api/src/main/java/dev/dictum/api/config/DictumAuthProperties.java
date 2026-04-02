package dev.dictum.api.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "dictum.auth")
@Validated
public class DictumAuthProperties {

  @Valid private final Admin admin = new Admin();

  public Admin getAdmin() {
    return admin;
  }

  public static class Admin {

    @NotBlank private String username;
    @NotBlank private String password;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
