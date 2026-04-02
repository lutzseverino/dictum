package dev.dictum.api.auth.factory;

import dev.dictum.api.auth.command.CreateSessionCommand;
import dev.dictum.api.generated.model.CreateSessionRequest;
import org.springframework.stereotype.Component;

@Component
public class SessionApiInputFactory {

  public CreateSessionCommand toCreateCommand(CreateSessionRequest request) {
    return new CreateSessionCommand(request.getUsername(), request.getPassword());
  }
}
