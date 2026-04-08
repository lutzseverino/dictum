package dev.dictum.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "dictum.content.repository=in-memory",
      TestAdminCredentials.USERNAME_PROPERTY,
      TestAdminCredentials.PASSWORD_PROPERTY
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class InMemoryHttpContractSupport {

  protected static final String CONTENT_TYPE_HEADER = "content-type";
  protected static final String MERGE_PATCH_JSON = "application/merge-patch+json";
  protected static final String PARAMS_FIELD = "params";
  protected static final String POSTS_SEGMENT = "posts";
  protected static final String DICTUM_BEGINS_SLUG = "dictum-begins";
  protected static final String REMOTE_CONTROLS_LATER_SLUG = "remote-controls-later";
  protected static final String UNKNOWN_SLUG = "unknown-slug";
  protected static final String ADMIN_TAG = "admin";
  protected static final String API_TAG = "api";
  protected static final String SESSION_PATH = path("api", "v1", "session");
  protected static final String POSTS_PATH = path("api", "v1", POSTS_SEGMENT);
  protected static final String REMOTE_CONTROLS_LATER_PATH =
      path("api", "v1", POSTS_SEGMENT, REMOTE_CONTROLS_LATER_SLUG);
  protected static final String SITE_SETTINGS_PATH = path("api", "v1", "settings", "site");
  protected static final String ADMIN_USERNAME = TestAdminCredentials.USERNAME;
  protected static final String ADMIN_PASSWORD = TestAdminCredentials.SECRET;

  protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  protected SessionHttpClient sessionHttpClient;

  @LocalServerPort private int port;

  @BeforeEach
  void setUpHttpClient() {
    sessionHttpClient = new SessionHttpClient(baseUrl(), objectMapper);
  }

  protected String baseUrl() {
    return "http://localhost:" + port;
  }

  protected static String path(String... segments) {
    return "/" + String.join("/", segments);
  }
}
