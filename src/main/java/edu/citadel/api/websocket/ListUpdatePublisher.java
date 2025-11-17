package edu.citadel.api.websocket;

import edu.citadel.dal.model.ListEntity;
import edu.citadel.dal.model.ListItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Service for publishing list and list item updates via WebSocket
 */
@Service
public class ListUpdatePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ListUpdatePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publish a list update to subscribers
     * @param listId The ID of the list
     * @param message The update message
     */
    public void publishListUpdate(Long listId, ListUpdateMessage message) {
        String topic = "/topic/list/" + listId;
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * Publish a list item update to the list subscribers
     * @param listId The ID of the list containing the item
     * @param message The update message
     */
    public void publishListItemUpdate(Long listId, ListItemUpdateMessage message) {
        String topic = "/topic/list/" + listId;
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * Convenience method to publish a list creation event
     */
    public void publishListCreated(ListEntity listEntity) {
        publishListUpdate(listEntity.getId(), 
                ListUpdateMessage.fromEntity(listEntity, "LIST-CREATED"));
    }

    /**
     * Convenience method to publish a list update event
     */
    public void publishListUpdated(ListEntity listEntity) {
        publishListUpdate(listEntity.getId(), 
                ListUpdateMessage.fromEntity(listEntity, "LIST-UPDATED"));
    }

    /**
     * Convenience method to publish a list deletion event
     */
    public void publishListDeleted(ListEntity listEntity) {
        publishListUpdate(listEntity.getId(), 
                ListUpdateMessage.fromEntity(listEntity, "LIST-DELETED"));
    }

    /**
     * Convenience method to publish a list item creation event
     */
    public void publishListItemCreated(ListItemEntity listItemEntity) {
        publishListItemUpdate(listItemEntity.getList().getId(), 
                ListItemUpdateMessage.fromEntity(listItemEntity, "CREATED"));
    }

    /**
     * Convenience method to publish a list item update event
     */
    public void publishListItemUpdated(ListItemEntity listItemEntity) {
        publishListItemUpdate(listItemEntity.getList().getId(), 
                ListItemUpdateMessage.fromEntity(listItemEntity, "UPDATED"));
    }

    /**
     * Convenience method to publish a list item deletion event
     */
    public void publishListItemDeleted(ListItemEntity listItemEntity) {
        publishListItemUpdate(listItemEntity.getList().getId(), 
                ListItemUpdateMessage.fromEntity(listItemEntity, "DELETED"));
    }
}
