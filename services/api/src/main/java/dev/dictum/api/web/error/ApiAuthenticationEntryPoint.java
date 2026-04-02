package dev.dictum.api.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ApiProblemResponseWriter apiProblemResponseWriter;

  ApiAuthenticationEntryPoint(ApiProblemResponseWriter apiProblemResponseWriter) {
    this.apiProblemResponseWriter = apiProblemResponseWriter;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authenticationException)
      throws IOException {
    apiProblemResponseWriter.write(
        request,
        response,
        ApiProblemSpec.unauthenticated(),
        "Authentication is required to access this resource.");
  }
}
