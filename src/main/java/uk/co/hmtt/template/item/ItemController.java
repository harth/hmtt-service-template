package uk.co.hmtt.template.item;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management API")
@SecurityRequirement(name = "basicAuth")
public class ItemController {

  private final ItemService itemService;

  @GetMapping
  @Operation(
      summary = "List all items",
      description = "Returns a paginated list of items. Use ?page=0&size=20&sort=name,asc")
  public Page<ItemResponse> getAllItems(
      @ParameterObject @PageableDefault(size = 20) final Pageable pageable) {
    log.debug("Request received: GET /items");
    return itemService.findAll(pageable);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create an item", description = "Persists a new item and returns it")
  public ItemResponse createItem(@Valid @RequestBody final ItemRequest item) {
    log.debug("Request received: POST /items — body: {}", item);
    return itemService.create(item);
  }
}
