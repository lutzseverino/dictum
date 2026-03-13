package dev.dictum.api.providers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/provider-jobs")
@Tag(name = "Provider Jobs", description = "Stub provider boundary endpoints")
public class ProviderJobsController {

  private final List<ProviderAdapter> providerAdapters;

  public ProviderJobsController(List<ProviderAdapter> providerAdapters) {
    this.providerAdapters = providerAdapters;
  }

  @GetMapping
  @Operation(summary = "List stub provider adapters and placeholder jobs")
  public ProviderJobsResponse listProviderJobs() {
    List<ProviderStatusResponse> providers =
        providerAdapters.stream()
            .map(
                adapter ->
                    new ProviderStatusResponse(
                        adapter.key(), adapter.mode(), adapter.supportedCommands()))
            .toList();

    return new ProviderJobsResponse(
        providers,
        List.of(
            new ProviderJobSummaryResponse(
                "provider-job-preview", "noop", "idle", "Awaiting live provider configuration.")));
  }

  public record ProviderJobsResponse(
      List<ProviderStatusResponse> providers, List<ProviderJobSummaryResponse> jobs) {}
}
