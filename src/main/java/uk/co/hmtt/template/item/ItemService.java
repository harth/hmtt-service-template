package uk.co.hmtt.template.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Business logic contract for item management. */
public interface ItemService {

  /** Return a paginated view of all items stored in the system. */
  Page<ItemResponse> findAll(Pageable pageable);

  /** Persist a new item and return its persisted representation. */
  ItemResponse create(ItemRequest request);
}
