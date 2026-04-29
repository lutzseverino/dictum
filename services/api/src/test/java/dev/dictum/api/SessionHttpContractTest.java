package dev.dictum.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import dev.dictum.api.generated.model.SessionResponse;
import dev.dictum.api.support.InMemoryHttpContractSupport;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class SessionHttpContractTest extends InMemoryHttpContractSupport {

  @Test
  void getSessionReturnsCurrentAuthenticatedSession() throws Exception {
    HttpResponse<String> loginResponse =
        sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    assertThat(loginResponse.statusCode()).isEqualTo(200);

    HttpResponse<String> response = sessionHttpClient.get(SESSION_PATH);

    assertThat(response.statusCode()).isEqualTo(200);

    SessionResponse session = objectMapper.readValue(response.body(), SessionResponse.class);
    assertThat(session.getUsername()).isEqualTo(ADMIN_USERNAME);
    assertThat(session.getCsrfToken()).isNotBlank();
  }

  @Test
  void createSessionRejectsInvalidCredentials() throws Exception {
    HttpResponse<String> response =
        sessionHttpClient.createSession(ADMIN_USERNAME, "wrong-password");

    assertThat(response.statusCode()).isEqualTo(401);
    assertThat(response.headers().firstValue(CONTENT_TYPE_HEADER))
        .hasValueSatisfying(
            value -> assertThat(value).contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

    JsonNode problem = objectMapper.readTree(response.body());
    assertThat(problem.get("code").asText()).isEqualTo("auth.invalid_credentials");
  }

  @Test
  void deleteSessionInvalidatesTheCurrentSession() throws Exception {
    sessionHttpClient.createSession(ADMIN_USERNAME, ADMIN_PASSWORD);

    HttpResponse<String> response = sessionHttpClient.delete(SESSION_PATH);

    assertThat(response.statusCode()).isEqualTo(204);

    HttpResponse<String> sessionResponse = sessionHttpClient.get(SESSION_PATH);
    assertThat(sessionResponse.statusCode()).isEqualTo(401);
  }
}
