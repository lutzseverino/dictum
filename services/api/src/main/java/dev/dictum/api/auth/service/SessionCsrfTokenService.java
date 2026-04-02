package dev.dictum.api.auth.service;

import dev.dictum.api.web.CurrentHttpRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionCsrfTokenService {

  private final CurrentHttpRequestContext currentHttpRequestContext;
  private final CsrfTokenRepository csrfTokenRepository;

  SessionCsrfTokenService(
      CurrentHttpRequestContext currentHttpRequestContext,
      CsrfTokenRepository csrfTokenRepository) {
    this.currentHttpRequestContext = currentHttpRequestContext;
    this.csrfTokenRepository = csrfTokenRepository;
  }

  public String token() {
    HttpServletRequest request = currentHttpRequestContext.request();
    HttpServletResponse response = currentHttpRequestContext.response();
    CsrfToken csrfToken = csrfTokenRepository.loadToken(request);

    if (csrfToken == null) {
      csrfToken = csrfTokenRepository.generateToken(request);
      csrfTokenRepository.saveToken(csrfToken, request, response);
    }

    request.setAttribute(CsrfToken.class.getName(), csrfToken);
    request.setAttribute(csrfToken.getParameterName(), csrfToken);
    return csrfToken.getToken();
  }
}
