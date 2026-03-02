package uk.co.hmtt.template.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

  @Mock private ItemRepository itemRepository;
  @Mock private ItemMapper itemMapper;

  @InjectMocks private ItemServiceImpl sut;

  // ── findAll ────────────────────────────────────────────────────────────

  @Test
  void findAll_delegatesToRepositoryAndMapper() {
    final Pageable pageable = PageRequest.of(0, 20);
    final Item entity = Item.builder().id(1L).name("Widget").description("Desc").build();
    final ItemResponse expected = new ItemResponse(1L, "Widget", "Desc");
    final Page<Item> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

    when(itemRepository.findAll(pageable)).thenReturn(entityPage);
    when(itemMapper.toResponse(entity)).thenReturn(expected);

    final Page<ItemResponse> result = sut.findAll(pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    assertThat(result.getContent().get(0).name()).isEqualTo("Widget");
    assertThat(result.getTotalElements()).isEqualTo(1L);
    verify(itemRepository).findAll(pageable);
    verify(itemMapper).toResponse(entity);
  }

  @Test
  void findAll_returnsEmptyPage_whenNoItemsExist() {
    final Pageable pageable = PageRequest.of(0, 20);
    when(itemRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

    final Page<ItemResponse> result = sut.findAll(pageable);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  // ── create ─────────────────────────────────────────────────────────────

  @Test
  void create_persistsEntityAndReturnsResponse() {
    final ItemRequest request = new ItemRequest("Gadget", "Cool gadget");
    final Item entity = Item.builder().name("Gadget").description("Cool gadget").build();
    final Item saved = Item.builder().id(42L).name("Gadget").description("Cool gadget").build();
    final ItemResponse expected = new ItemResponse(42L, "Gadget", "Cool gadget");

    when(itemMapper.toEntity(request)).thenReturn(entity);
    when(itemRepository.save(entity)).thenReturn(saved);
    when(itemMapper.toResponse(saved)).thenReturn(expected);

    final ItemResponse result = sut.create(request);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(42L);
    assertThat(result.name()).isEqualTo("Gadget");
    verify(itemMapper).toEntity(request);
    verify(itemRepository).save(any(Item.class));
    verify(itemMapper).toResponse(saved);
  }
}
