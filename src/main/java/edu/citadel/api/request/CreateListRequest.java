package edu.citadel.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateListRequest {

    @Schema(
            example = "Grocery List",
            description = "Optional custom title for the list. If omitted or blank, defaults to 'New List'."
    )
    private String title;
}

