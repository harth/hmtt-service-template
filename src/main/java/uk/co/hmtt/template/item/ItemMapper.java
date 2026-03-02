package uk.co.hmtt.template.item;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Item} entities and DTOs. The generated
 * implementation is a Spring-managed bean.
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {

  /** Map a persisted entity to its API response representation. */
  ItemResponse toResponse(Item item);

  /** Map a list of persisted entities to a list of API responses. */
  List<ItemResponse> toResponseList(List<Item> items);

  /** Map an incoming API request to a new (transient) entity. The {@code id} is DB-generated. */
  @Mapping(target = "id", ignore = true)
  Item toEntity(ItemRequest request);
}
