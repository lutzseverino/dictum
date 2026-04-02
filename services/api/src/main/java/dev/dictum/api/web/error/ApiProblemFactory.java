package dev.dictum.api.web.error;

import dev.dictum.api.generated.model.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ApiProblemFactory {

  public ProblemDetails create(
      ApiProblemSpec spec, @Nullable String detail, HttpServletRequest request) {
    return new ProblemDetails(spec.title(), spec.status().value(), spec.code(), spec.params())
        .type(spec.type())
        .detail(detail)
        .instance(URI.create(request.getRequestURI()).toString());
  }
}
