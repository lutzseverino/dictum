package dev.dictum.api.providers;

import java.util.List;

public interface ProviderAdapter {

  String key();

  String mode();

  List<String> supportedCommands();
}
