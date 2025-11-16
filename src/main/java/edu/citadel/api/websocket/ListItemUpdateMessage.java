package edu.citadel.api.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.citadel.dal.model.ListItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket message for list item updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListItemUpdateMessage {

    // Don't think we will need this in UI
    @JsonProperty("event_type")
    private String eventType; // "CREATED", "UPDATED", "DELETED"
    
    @JsonProperty("list_id")
    private Long listId;
    
    @JsonProperty("item_id")
    private Long itemId;
    
    @JsonProperty("item_name")
    private String itemName;
    
    @JsonProperty("item_description")
    private String itemDescription;
    
    @JsonProperty("completed")
    private Boolean completed;
    
    @JsonProperty("completed_on")
    private LocalDateTime completedOn;
    
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * Create an update message from a ListItemEntity
     */
    public static ListItemUpdateMessage fromEntity(ListItemEntity entity, String eventType) {
        return ListItemUpdateMessage.builder()
                .eventType(eventType)
                .listId(entity.getList().getId())
                .itemId(entity.getId())
                .itemName(entity.getListItemName())
                .itemDescription(entity.getListItemDesc())
                .completed(entity.getCompleted())
                .completedOn(entity.getCompletedOn())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
