package uk.co.hmtt.template.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.co.hmtt.template.common.AppConfig;

@WebMvcTest(ItemController.class)
@Import(AppConfig.class)
class ItemControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ItemService itemService;
  // Required because AppConfig.timedAspect() injects MeterRegistry,
  // which is not auto-configured in the @WebMvcTest slice.
  @MockitoBean private MeterRegistry meterRegistry;

  // ── GET /items ─────────────────────────────────────────────────────────

  @Test
  @WithMockUser
  void getAllItems_returns200WithItemList() throws Exception {
    final ItemResponse item = new ItemResponse(1L, "Widget", "Desc");
    when(itemService.findAll(any())).thenReturn(new PageImpl<>(List.of(item)));

    final MvcResult result =
        mockMvc
            .perform(get("/items").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].name").value("Widget"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andReturn();

    assertThat(result.getResponse().getContentAsString()).contains("Widget");
  }

  @Test
  void getAllItems_returns401_whenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/items")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void getAllItems_respectsPageableParameters() throws Exception {
    when(itemService.findAll(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0));

    mockMvc
        .perform(
            get("/items").param("page", "2").param("size", "5").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pageable.pageNumber").value(2))
        .andExpect(jsonPath("$.pageable.pageSize").value(5));
  }

  // ── POST /items ────────────────────────────────────────────────────────

  @Test
  @WithMockUser
  void createItem_withValidBody_returns201() throws Exception {
    final ItemRequest request = new ItemRequest("New Widget", "A brand new widget");
    final ItemResponse response = new ItemResponse(5L, "New Widget", "A brand new widget");
    when(itemService.create(any())).thenReturn(response);

    final MvcResult result =
        mockMvc
            .perform(
                post("/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    final String body = result.getResponse().getContentAsString();
    assertThat(body).contains("New Widget");
    assertThat(body).contains("5");
  }

  @Test
  @WithMockUser
  void createItem_withBlankName_returns400() throws Exception {
    final ItemRequest invalid = new ItemRequest("", "Some desc");

    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void createItem_withWhitespaceOnlyName_returns400() throws Exception {
    final ItemRequest invalid = new ItemRequest("   ", "Some desc");

    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  @WithMockUser
  void createItem_withBlankDescription_returns400() throws Exception {
    final ItemRequest invalid = new ItemRequest("Valid name", "");

    mockMvc
        .perform(
            post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void createItem_withMalformedJson_returns400() throws Exception {
    mockMvc
        .perform(
            post("/items").contentType(MediaType.APPLICATION_JSON).content("{ not valid json }"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
  }

  // ── GlobalExceptionHandler ─────────────────────────────────────────────

  @Test
  @WithMockUser
  void getAllItems_returns404_whenEntityNotFound() throws Exception {
    when(itemService.findAll(any())).thenThrow(new EntityNotFoundException("Item not found"));

    mockMvc
        .perform(get("/items").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Item not found"));
  }

  @Test
  @WithMockUser
  void getAllItems_returns500_onUnexpectedException() throws Exception {
    when(itemService.findAll(any())).thenThrow(new RuntimeException("Unexpected failure"));

    mockMvc
        .perform(get("/items").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
