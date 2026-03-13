package dev.dictum.api.providers;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NoOpProviderAdapter implements ProviderAdapter {

  @Override
  public String key() {
    return "noop";
  }

  @Override
  public String mode() {
    return "stub";
  }

  @Override
  public List<String> supportedCommands() {
    return List.of("request_text_change", "request_visual_change");
  }
}
