package uk.co.hmtt.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.co.hmtt.template.item.ItemRequest;

/*
 * ─── WireMock stub example (for future external-API stubbing) ─────────────────
 *
 * @RegisterExtension
 * static WireMockExtension wm = WireMockExtension.newInstance()
 *     .options(wireMockConfig().dynamicPort())
 *     .build();
 *
 * Then inside a test method:
 *   wm.stubFor(get(urlEqualTo("/external/api"))
 *       .willReturn(aResponse()
 *           .withStatus(200)
 *           .withHeader("Content-Type", "application/json")
 *           .withBody("[{\"id\":1,\"name\":\"Stub Widget\"}]")));
 *
 * The service under test would need to be configured with:
 *   @DynamicPropertySource
 *   static void configureProperties(DynamicPropertyRegistry registry) {
 *       registry.add("external.api.base-url", wm::baseUrl);
 *   }
 * ─────────────────────────────────────────────────────────────────────────────
 */

/**
 * Functional tests that start the full Spring context on a random port and drive the application
 * through its HTTP surface.
 *
 * <p>Uses the {@code local} profile so that H2 is initialised with seed data and human-readable
 * logging is active.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ItemFunctionalTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  // ── GET /items ──────────────────────────────────────────────────────────

  @Test
  void getItems_withValidCredentials_returns200AndNonEmptyBody() {
    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .getForEntity("http://localhost:" + port + "/items", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsKey("content");

    @SuppressWarnings("unchecked")
    final List<?> content = (List<?>) response.getBody().get("content");
    assertThat(content).isNotEmpty();
  }

  @Test
  void getItems_withoutCredentials_returns401() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + "/items", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getItems_responseBodyContainsExpectedFields() {
    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .getForEntity("http://localhost:" + port + "/items", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> items =
        (List<Map<String, Object>>) response.getBody().get("content");

    assertThat(items).isNotNull().isNotEmpty();

    final Map<String, Object> first = items.get(0);
    assertThat(first).containsKeys("id", "name", "description");
  }

  @Test
  void getItems_withPageParameters_returnsPageMetadata() {
    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .getForEntity("http://localhost:" + port + "/items?page=0&size=2", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsKeys("content", "totalElements", "totalPages");

    @SuppressWarnings("unchecked")
    final List<?> content = (List<?>) response.getBody().get("content");
    assertThat(content).hasSize(2);
  }

  // ── POST /items ─────────────────────────────────────────────────────────

  @Test
  void createItem_withValidCredentials_returns201AndCreatedItem() {
    final ItemRequest request = new ItemRequest("Functional Widget", "Created in functional test");
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<ItemRequest> entity = new HttpEntity<>(request, headers);

    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .postForEntity("http://localhost:" + port + "/items", entity, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).containsKeys("id", "name", "description");
    assertThat(response.getBody().get("name")).isEqualTo("Functional Widget");
  }

  @Test
  void createItem_withoutCredentials_returns401() {
    final ItemRequest request = new ItemRequest("Widget", "Desc");
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<ItemRequest> entity = new HttpEntity<>(request, headers);

    final ResponseEntity<String> response =
        restTemplate.postForEntity("http://localhost:" + port + "/items", entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void createItem_withBlankName_returns400() {
    final ItemRequest invalid = new ItemRequest("", "Some description");
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<ItemRequest> entity = new HttpEntity<>(invalid, headers);

    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .postForEntity("http://localhost:" + port + "/items", entity, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsKey("status");
    assertThat(response.getBody().get("status")).isEqualTo(400);
  }

  @Test
  void createItem_withWhitespaceOnlyName_returns400() {
    final ItemRequest invalid = new ItemRequest("   ", "Some description");
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    final HttpEntity<ItemRequest> entity = new HttpEntity<>(invalid, headers);

    @SuppressWarnings("unchecked")
    final ResponseEntity<Map> response =
        restTemplate
            .withBasicAuth("user", "password")
            .postForEntity("http://localhost:" + port + "/items", entity, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().get("status")).isEqualTo(400);
  }

  // ── Actuator ────────────────────────────────────────────────────────────

  @Test
  void actuatorHealth_isAccessibleWithoutAuth() {
    final ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
