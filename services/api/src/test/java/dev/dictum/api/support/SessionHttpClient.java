package dev.dictum.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dictum.api.generated.model.SessionResponse;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SessionHttpClient {

  private static final String SESSION_PASSWORD_FIELD = "password";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private String csrfToken;

  public SessionHttpClient(String baseUrl, ObjectMapper objectMapper) {
    this.baseUrl = baseUrl;
    this.objectMapper = objectMapper;
    this.httpClient =
        HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
  }

  public HttpResponse<String> createSession(String username, String password)
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        request(
            HttpMethod.POST,
            "/api/v1/session",
            "application/json",
            """
            {
              "username": "%s",
              "%s": "%s"
            }
            """
                .formatted(username, SESSION_PASSWORD_FIELD, password),
            false);

    if (response.statusCode() == 200) {
      csrfToken = objectMapper.readValue(response.body(), SessionResponse.class).getCsrfToken();
    }

    return response;
  }

  public HttpResponse<String> getAuthenticated(String path, String username, String password)
      throws IOException, InterruptedException {
    createSession(username, password);
    return get(path);
  }

  public HttpResponse<String> get(String path) throws IOException, InterruptedException {
    return request(HttpMethod.GET, path, null, null, false);
  }

  public HttpResponse<String> post(String path, String contentType, String body)
      throws IOException, InterruptedException {
    return request(HttpMethod.POST, path, contentType, body, true);
  }

  public HttpResponse<String> patch(String path, String contentType, String body)
      throws IOException, InterruptedException {
    return request(HttpMethod.PATCH, path, contentType, body, true);
  }

  public HttpResponse<String> patchWithoutCsrf(String path, String contentType, String body)
      throws IOException, InterruptedException {
    return request(HttpMethod.PATCH, path, contentType, body, false);
  }

  public HttpResponse<String> delete(String path) throws IOException, InterruptedException {
    return request(HttpMethod.DELETE, path, null, null, true);
  }

  public HttpResponse<String> requestAuthenticated(
      HttpMethod method,
      String path,
      String contentType,
      String body,
      String username,
      String password)
      throws IOException, InterruptedException {
    createSession(username, password);
    return request(method, path, contentType, body, true);
  }

  public HttpResponse<String> request(
      HttpMethod method, String path, String contentType, String body, boolean includeCsrfToken)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(baseUrl + path));

    if (contentType != null) {
      builder.header("Content-Type", contentType);
    }

    if (includeCsrfToken && csrfToken != null) {
      builder.header("X-CSRF-TOKEN", csrfToken);
    }

    HttpRequest request =
        switch (method) {
          case POST ->
              builder
                  .POST(
                      body == null
                          ? HttpRequest.BodyPublishers.noBody()
                          : HttpRequest.BodyPublishers.ofString(body))
                  .build();
          case PATCH ->
              builder
                  .method(
                      method.value,
                      body == null
                          ? HttpRequest.BodyPublishers.noBody()
                          : HttpRequest.BodyPublishers.ofString(body))
                  .build();
          case DELETE -> builder.DELETE().build();
          default -> builder.GET().build();
        };

    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PATCH("PATCH"),
    DELETE("DELETE");

    private final String value;

    HttpMethod(String value) {
      this.value = value;
    }
  }
}
