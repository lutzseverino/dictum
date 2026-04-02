package dev.dictum.api.auth.controller;

import dev.dictum.api.auth.factory.SessionApiInputFactory;
import dev.dictum.api.auth.mapper.SessionApiMapper;
import dev.dictum.api.auth.service.SessionCommandService;
import dev.dictum.api.auth.service.SessionQueryService;
import dev.dictum.api.generated.api.SessionApi;
import dev.dictum.api.generated.model.CreateSessionRequest;
import dev.dictum.api.generated.model.SessionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class SessionController implements SessionApi {

  private final SessionApiInputFactory sessionApiInputFactory;
  private final SessionApiMapper sessionApiMapper;
  private final SessionCommandService sessionCommandService;
  private final SessionQueryService sessionQueryService;

  SessionController(
      SessionApiInputFactory sessionApiInputFactory,
      SessionApiMapper sessionApiMapper,
      SessionCommandService sessionCommandService,
      SessionQueryService sessionQueryService) {
    this.sessionApiInputFactory = sessionApiInputFactory;
    this.sessionApiMapper = sessionApiMapper;
    this.sessionCommandService = sessionCommandService;
    this.sessionQueryService = sessionQueryService;
  }

  @Override
  public ResponseEntity<SessionResponse> create(CreateSessionRequest createSessionRequest) {
    return ResponseEntity.ok(
        sessionApiMapper.toResponse(
            sessionCommandService.create(
                sessionApiInputFactory.toCreateCommand(createSessionRequest))));
  }

  @Override
  public ResponseEntity<Void> delete(String xCsrfToken) {
    sessionCommandService.delete();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<SessionResponse> get() {
    return ResponseEntity.ok(sessionApiMapper.toResponse(sessionQueryService.get()));
  }
}
