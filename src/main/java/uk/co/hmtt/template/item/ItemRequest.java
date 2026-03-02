package uk.co.hmtt.template.item;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request payload for creating a new item. */
@Schema(description = "Request payload for creating an item")
public record ItemRequest(
    @NotBlank(message = "Name must not be blank")
        @Schema(description = "Name of the item", example = "Widget A")
        String name,
    @NotBlank(message = "Description must not be blank")
        @Schema(description = "Description of the item", example = "A very useful widget")
        String description) {}
