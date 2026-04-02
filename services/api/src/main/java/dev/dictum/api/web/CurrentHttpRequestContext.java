package dev.dictum.api.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentHttpRequestContext {

  public HttpServletRequest request() {
    return currentAttributes().getRequest();
  }

  public HttpServletResponse response() {
    HttpServletResponse response = currentAttributes().getResponse();

    if (response == null) {
      throw new IllegalStateException("No current HTTP response is available");
    }

    return response;
  }

  private ServletRequestAttributes currentAttributes() {
    return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
  }
}
