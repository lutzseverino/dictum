package dev.dictum.api.auth.model.state;

public record SessionState(String username, String csrfToken) {}
