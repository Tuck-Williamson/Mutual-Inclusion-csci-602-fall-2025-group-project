package edu.citadel.api.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.citadel.dal.model.ListEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * WebSocket message for list updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListUpdateMessage {
    
    @JsonProperty("event_type")
    private String eventType; // "CREATED", "UPDATED", "DELETED"
    
    @JsonProperty("list_id")
    private Long listId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("completed_on")
    private Instant completedOn;
    
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("owner_user_id")
    private String ownerUserId;

    @JsonProperty("recipient_user_id")
    private String recipientUserId;

    /**
     * Create an update message from a ListEntity
     */
    public static ListUpdateMessage fromEntity(ListEntity entity, String eventType) {
        return ListUpdateMessage.builder()
                .eventType(eventType)
                .listId(entity.getId())
                .title(entity.getTitle())
                .completedOn(entity.getCompletedOn())
                .timestamp(System.currentTimeMillis())
                .ownerUserId(null)
                .recipientUserId(null)
                .build();
    }
}
