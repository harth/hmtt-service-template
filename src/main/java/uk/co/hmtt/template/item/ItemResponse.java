package uk.co.hmtt.template.item;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response payload representing a persisted item. */
@Schema(description = "Response payload representing an item")
public record ItemResponse(
    @Schema(description = "Unique identifier of the item", example = "1") Long id,
    @Schema(description = "Name of the item", example = "Widget A") String name,
    @Schema(description = "Description of the item", example = "A very useful widget")
        String description) {}
