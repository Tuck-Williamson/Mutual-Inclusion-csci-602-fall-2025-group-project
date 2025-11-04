package edu.citadel.api.request;

import lombok.Data;

@Data
public class ListItemRequestBody {
    private String listItemName;
    private String listItemDescription;
}
