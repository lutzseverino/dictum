package dev.dictum.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DictumApiApplicationTests {

	@LocalServerPort
	private int port;

	@Test
	void contextLoads() {
	}

	@Test
	void apiRootExposesRouteGroups() throws Exception {
		HttpResponse<String> response = get("/api/v1");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"routeGroups\":[\"posts\",\"settings\",\"commands\",\"provider-jobs\"]");
		assertThat(response.body()).contains("\"docsPath\":\"/swagger-ui.html\"");
	}

	@Test
	void postsEndpointReturnsStubbedEntries() throws Exception {
		HttpResponse<String> response = get("/api/v1/posts");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"slug\":\"dictum-begins\"");
		assertThat(response.body()).contains("\"hasStylesheet\":true");
	}

	@Test
	void settingsEndpointReturnsSiteSettings() throws Exception {
		HttpResponse<String> response = get("/api/v1/settings/site");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"source\":\"external-content-repository\"");
	}

	@Test
	void commandsEndpointReturnsPlannedControls() throws Exception {
		HttpResponse<String> response = get("/api/v1/commands");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"name\":\"create_post\"");
		assertThat(response.body()).contains("\"status\":\"planned\"");
	}

	@Test
	void providerJobsEndpointExposesNoOpProvider() throws Exception {
		HttpResponse<String> response = get("/api/v1/provider-jobs");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"provider\":\"noop\"");
		assertThat(response.body()).contains("\"state\":\"idle\"");
	}

	private HttpResponse<String> get(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + path))
			.GET()
			.build();

		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}

}
