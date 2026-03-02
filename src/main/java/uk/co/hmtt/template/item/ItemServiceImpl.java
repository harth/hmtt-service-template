package uk.co.hmtt.template.item;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default implementation of {@link ItemService}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  @Override
  @Transactional(readOnly = true)
  @Timed(value = "item.findAll", description = "Time taken to fetch all items")
  public Page<ItemResponse> findAll(final Pageable pageable) {
    log.debug("Fetching items page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return itemRepository.findAll(pageable).map(itemMapper::toResponse);
  }

  @Override
  @Transactional
  @Timed(value = "item.create", description = "Time taken to create an item")
  public ItemResponse create(final ItemRequest request) {
    log.debug("Creating item with name: '{}'", request.name());
    final Item entity = itemMapper.toEntity(request);
    final Item saved = itemRepository.save(entity);
    log.debug("Item persisted with id: {}", saved.getId());
    return itemMapper.toResponse(saved);
  }
}
