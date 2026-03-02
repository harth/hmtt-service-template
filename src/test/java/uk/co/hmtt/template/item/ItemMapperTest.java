package uk.co.hmtt.template.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the MapStruct-generated {@link ItemMapperImpl}. Instantiated directly — no Spring
 * context required.
 */
class ItemMapperTest {

  private final ItemMapper mapper = new ItemMapperImpl();

  // ── toResponse ─────────────────────────────────────────────────────────

  @Test
  void toResponse_mapsAllFieldsFromEntity() {
    final Item entity = Item.builder().id(7L).name("Widget A").description("Desc A").build();

    final ItemResponse response = mapper.toResponse(entity);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(7L);
    assertThat(response.name()).isEqualTo("Widget A");
    assertThat(response.description()).isEqualTo("Desc A");
  }

  @Test
  void toResponse_returnsNull_whenEntityIsNull() {
    assertThat(mapper.toResponse(null)).isNull();
  }

  // ── toResponseList ─────────────────────────────────────────────────────

  @Test
  void toResponseList_mapsEveryElement() {
    final List<Item> entities =
        List.of(
            Item.builder().id(1L).name("A").description("Desc A").build(),
            Item.builder().id(2L).name("B").description("Desc B").build());

    final List<ItemResponse> responses = mapper.toResponseList(entities);

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).id()).isEqualTo(1L);
    assertThat(responses.get(1).name()).isEqualTo("B");
  }

  @Test
  void toResponseList_returnsEmptyList_whenInputIsEmpty() {
    assertThat(mapper.toResponseList(List.of())).isEmpty();
  }

  @Test
  void toResponseList_returnsNull_whenInputIsNull() {
    assertThat(mapper.toResponseList(null)).isNull();
  }

  // ── toEntity ───────────────────────────────────────────────────────────

  @Test
  void toEntity_mapsRequestFieldsToEntity() {
    final ItemRequest request = new ItemRequest("Gadget X", "Cool gadget");

    final Item entity = mapper.toEntity(request);

    assertThat(entity).isNotNull();
    assertThat(entity.getName()).isEqualTo("Gadget X");
    assertThat(entity.getDescription()).isEqualTo("Cool gadget");
    // id must be null — it is generated on persist
    assertThat(entity.getId()).isNull();
  }

  @Test
  void toEntity_returnsNull_whenRequestIsNull() {
    assertThat(mapper.toEntity(null)).isNull();
  }
}
