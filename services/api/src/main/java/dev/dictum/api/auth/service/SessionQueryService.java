package dev.dictum.api.auth.service;

import dev.dictum.api.auth.model.state.SessionState;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionQueryService {

  private final SessionCsrfTokenService sessionCsrfTokenService;

  SessionQueryService(SessionCsrfTokenService sessionCsrfTokenService) {
    this.sessionCsrfTokenService = sessionCsrfTokenService;
  }

  public SessionState get() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated session is available");
    }

    if (authentication instanceof AnonymousAuthenticationToken) {
      throw new IllegalStateException("Anonymous access does not expose a control-plane session");
    }

    return new SessionState(authentication.getName(), sessionCsrfTokenService.token());
  }
}
