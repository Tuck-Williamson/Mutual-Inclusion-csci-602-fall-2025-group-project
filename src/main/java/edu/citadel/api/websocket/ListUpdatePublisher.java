package edu.citadel.api.websocket;

import edu.citadel.dal.model.ListEntity;
import edu.citadel.dal.model.ListItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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

    /************************************************************************
     * Convenience methods for publishing sharing events.
     ************************************************************************/

    public void publishShareCreated(ListEntity listEntity) {
        publishListUpdate(listEntity.getId(),
                ListUpdateMessage.fromEntity(listEntity, "SHARE-CREATED"));
        // No user-directed message needed. The owner gets REST api confirmation,
        // and the recipient is only defined by whom the owner shares the link with.
    }

    /**
     * This method publishes a SHARE-DELETED event for when the owner deletes
     * a sharing offer for a specific user.
     * @param listEntity The list entity that was shared.
     * @param owner_name The name of the shared list's owner.
     * @param recipient_name The name of the user that was removed.
     */
    public void publishShareDeleted(ListEntity listEntity, String owner_name, String recipient_name) {
        ListUpdateMessage msg = ListUpdateMessage.fromEntity(listEntity, "SHARE-DELETED");
        msg.setOwnerUserId(owner_name);
        msg.setRecipientUserId(recipient_name);

        publishListUpdate(listEntity.getId(), msg);

        // Send a message to the recipient that was removed.
        String topic = "/topic/user/" + recipient_name;
        messagingTemplate.convertAndSend(topic, msg);
    }

    /**
     * This method publishes a SHARE-REJECTED event for when the non-owner removes
     * themselves from a shared list.
     * @param listEntity The list entity that was shared.
     * @param owner_name The name of the shared list's owner.
     * @param recipient_name The name of the user that removed themselves.
     */
    public void publishShareRejected(ListEntity listEntity, String owner_name, String recipient_name) {
        ListUpdateMessage msg = ListUpdateMessage.fromEntity(listEntity, "SHARE-REJECTED");
        msg.setOwnerUserId(owner_name);
        msg.setRecipientUserId(recipient_name);
        publishListUpdate(listEntity.getId(), msg);

        // Send a message to the owner that the share was rejected.
        String topic = "/topic/user/" + owner_name;
        messagingTemplate.convertAndSend(topic, msg);
    }

    /**
     * This method publishes a SHARE-ACCEPTED event for when the non-owner accepts
     * a shared list.
     * @param listEntity The list entity that was shared.
     * @param owner_name The name of the shared list's owner.
     * @param recipient_name The name of the user that accepted the share.
     */
    public void publishShareAccepted(ListEntity listEntity, String owner_name, String recipient_name) {
        ListUpdateMessage msg = ListUpdateMessage.fromEntity(listEntity, "SHARE-ACCEPTED");
        msg.setOwnerUserId(owner_name);
        msg.setRecipientUserId(recipient_name);

        publishListUpdate(listEntity.getId(), msg);

        // Send a message to the owner that the share was rejected.
        String topic = "/topic/user/" + owner_name;
        messagingTemplate.convertAndSend(topic, msg);
    }


    /************************************************************************
     * Convenience methods for publishing list item events.
     ************************************************************************/

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
