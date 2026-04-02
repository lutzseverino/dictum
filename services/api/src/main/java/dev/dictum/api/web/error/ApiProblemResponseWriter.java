package dev.dictum.api.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiProblemResponseWriter {

  private final ApiProblemFactory apiProblemFactory;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  ApiProblemResponseWriter(ApiProblemFactory apiProblemFactory) {
    this.apiProblemFactory = apiProblemFactory;
  }

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      ApiProblemSpec spec,
      @Nullable String detail)
      throws IOException {
    response.setStatus(spec.status().value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(), apiProblemFactory.create(spec, detail, request));
  }
}
