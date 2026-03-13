package dev.dictum.api.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class RequestBodyCachingFilter extends OncePerRequestFilter {

  private static final int REQUEST_CACHE_LIMIT = 1024 * 1024;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request instanceof ContentCachingRequestWrapper) {
      filterChain.doFilter(request, response);
      return;
    }

    // PATCH updates can legitimately carry substantial markdown bodies, so the
    // cached request document needs enough headroom for field-presence checks.
    filterChain.doFilter(new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT), response);
  }
}
