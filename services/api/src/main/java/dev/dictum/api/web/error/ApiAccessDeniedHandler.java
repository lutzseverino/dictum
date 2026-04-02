package dev.dictum.api.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ApiProblemResponseWriter apiProblemResponseWriter;

  ApiAccessDeniedHandler(ApiProblemResponseWriter apiProblemResponseWriter) {
    this.apiProblemResponseWriter = apiProblemResponseWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    if (accessDeniedException instanceof CsrfException) {
      if (isAnonymousRequest()) {
        apiProblemResponseWriter.write(
            request,
            response,
            ApiProblemSpec.unauthenticated(),
            "Authentication is required to access this resource.");
        return;
      }

      apiProblemResponseWriter.write(
          request, response, ApiProblemSpec.csrfInvalid(), "The CSRF token is missing or invalid.");
      return;
    }

    apiProblemResponseWriter.write(
        request,
        response,
        ApiProblemSpec.forbidden(),
        "The current session is not allowed to perform this action.");
  }

  private static boolean isAnonymousRequest() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken;
  }
}
