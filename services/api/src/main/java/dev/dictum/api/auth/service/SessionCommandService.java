package dev.dictum.api.auth.service;

import dev.dictum.api.auth.command.CreateSessionCommand;
import dev.dictum.api.auth.error.InvalidCredentialsException;
import dev.dictum.api.auth.model.state.SessionState;
import dev.dictum.api.web.CurrentHttpRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionCommandService {

  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final CurrentHttpRequestContext currentHttpRequestContext;
  private final SessionCsrfTokenService sessionCsrfTokenService;

  SessionCommandService(
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository,
      CurrentHttpRequestContext currentHttpRequestContext,
      SessionCsrfTokenService sessionCsrfTokenService) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.currentHttpRequestContext = currentHttpRequestContext;
    this.sessionCsrfTokenService = sessionCsrfTokenService;
  }

  public SessionState create(CreateSessionCommand command) {
    HttpServletRequest request = currentHttpRequestContext.request();
    HttpServletResponse response = currentHttpRequestContext.response();

    Authentication authentication;
    try {
      authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  command.username(), command.password()));
    } catch (BadCredentialsException exception) {
      throw new InvalidCredentialsException();
    }

    request.getSession(true);
    request.changeSessionId();

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);

    return new SessionState(authentication.getName(), sessionCsrfTokenService.token());
  }

  public void delete() {
    HttpServletRequest request = currentHttpRequestContext.request();
    HttpServletResponse response = currentHttpRequestContext.response();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    new SecurityContextLogoutHandler().logout(request, response, authentication);
  }
}
