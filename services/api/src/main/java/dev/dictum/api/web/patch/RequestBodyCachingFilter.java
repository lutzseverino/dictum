package dev.dictum.api.web.patch;

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

  private static final int REQUEST_CACHE_LIMIT = Integer.MAX_VALUE;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"PATCH".equalsIgnoreCase(request.getMethod());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request instanceof ContentCachingRequestWrapper) {
      filterChain.doFilter(request, response);
      return;
    }

    // PATCH updates can legitimately carry substantial markdown bodies. Until
    // the API publishes an explicit payload-size contract, the cache must not
    // impose a smaller hidden truncation limit than the endpoint itself.
    filterChain.doFilter(new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT), response);
  }
}
