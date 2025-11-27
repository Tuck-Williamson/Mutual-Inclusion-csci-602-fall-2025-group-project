package edu.citadel.api;

import edu.citadel.api.request.CreateListRequest;
import edu.citadel.api.request.ListItemRequestBody;
import edu.citadel.api.websocket.ListUpdatePublisher;
import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.ListItemEntityRepository;
import edu.citadel.dal.ShareRepository;
import edu.citadel.dal.model.*;
import edu.citadel.utils.AccountDelegate;
import lombok.Data;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.time.Instant;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@RestController
@RequestMapping("/list")
public class ListController {

    private final ListEntityRepository listEntityRepository;

    private final ListItemEntityRepository listItemEntityRepository;
    
    private final ListUpdatePublisher listUpdatePublisher;

    @Setter
    @Autowired
    private AccountDelegate accountDelegate;

    @Setter
    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    public ListController(
            final ListEntityRepository listEntityRepository,
            final ListItemEntityRepository listItemEntityRepository,
            final ListUpdatePublisher listUpdatePublisher) {
        this.listEntityRepository = listEntityRepository;
        this.listItemEntityRepository = listItemEntityRepository;
        this.listUpdatePublisher = listUpdatePublisher;
    }

    private Account getCurrentAccount() {
        return accountDelegate.getCurrentAccount();
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListEntity>> getAllLists(@AuthenticationPrincipal OAuth2User principal
    ) {
        Account currentAccount = getCurrentAccount();
        List<ListEntity> listEntities =
                listEntityRepository.findByAccount(currentAccount);
        return ResponseEntity.ok(listEntities);
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> createList(
            @RequestBody(required = false) CreateListRequest body,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Account currentAccount = getCurrentAccount();

        ListEntity list = new ListEntity();

        if (body != null && body.getTitle() != null && !body.getTitle().isBlank()) {
            list.setTitle(body.getTitle());
        }

        list.setOwnerUsername(currentAccount.getUsername());// TODO: clean up these references
        list.setAccount(currentAccount);

        ListEntity savedList = listEntityRepository.save(list);
        listUpdatePublisher.publishListCreated(savedList);
        return ResponseEntity.ok(savedList);
    }

    /**
     * Retrieves a list by its ID
     * @param listId The ID of the list to retrieve
     * @return ResponseEntity containing the ListEntity if found, 404 if not found
     */
    @GetMapping(
            value = "/{listId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> viewList(@PathVariable Long listId) {
        try {
            return listEntityRepository.findById(listId)
                    .map(listEntity -> {
                        Set<ShareEntity> shares = listEntity.getShares();
                        Set<ShareEntity> shares_uname = new HashSet<>();
                        for (ShareEntity share : shares) {
                            if (share.getUser() == null) {
                                share.setUsername(null);
                                shares_uname.add(share);
                            }
                            else {
                                share.setUsername(share.getUser().getUsername());
                                shares_uname.add(share);
                            }
                        }
                        listEntity.setShares(shares_uname);
                        return ResponseEntity.ok(listEntity);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing list's title
     * @param listId The ID of the list to update
     * @param request UpdateListRequest containing the new title
     * @return ResponseEntity containing the updated ListEntity if found, 404 if not found
     */
    @PutMapping(
            value = "/{listId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> editList(@PathVariable Long listId,
                                               @RequestBody UpdateListRequest request) {
        try {
            return this.listEntityRepository.findById(listId)
                    .map(existingList -> {
                        // Update title if provided
                        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                            if (request.getTitle().length() > 50) {
                                return ResponseEntity.badRequest().<ListEntity>build();
                            }
                            existingList.setTitle(request.getTitle().trim());
                        }

                        // Update completed_on if provided
                        if (request.getCompletedOn() != null) {
                            existingList.setCompletedOn(request.getCompletedOn());
                        }

                        ListEntity updatedList = this.listEntityRepository.save(existingList);
                        listUpdatePublisher.publishListUpdated(updatedList);
                        return ResponseEntity.ok(updatedList);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Inner class for the update list request body
     */
    @Data
    public static class UpdateListRequest {
        private String title;
        private java.time.Instant completedOn;
    }


    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListEntity> deleteList(@PathVariable Long id) {
        return listEntityRepository.findById(id).map(list -> {
            Hibernate.initialize(list.getListItems()); // initialize the list
            listEntityRepository.delete(list);
            listUpdatePublisher.publishListDeleted(list);
            return ResponseEntity.ok(list);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(
            value = "/{listId}/item",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListItemEntity> addListItem(@PathVariable Long listId, @RequestBody(required = false) ListItemRequestBody listItem) {
        Optional<ListEntity> optionalList = listEntityRepository.findById(listId);
        return optionalList.map(listEntity -> {
            ListItemEntity listItemEntity = new ListItemEntity();
            listItemEntity.setCompleted(false);
            listItemEntity.setList(listEntity);
            Optional.ofNullable(listItem)
                    .ifPresent(listItemRequest -> {
                        Optional.ofNullable(
                                listItemRequest.getListItemName()
                        ).ifPresent(listItemEntity::setListItemName);
                        Optional.ofNullable(
                                listItemRequest.getListItemDescription()
                        ).ifPresent(listItemEntity::setListItemDesc);
            });
            ListItemEntity savedItem = listItemEntityRepository.save(listItemEntity);
            listUpdatePublisher.publishListItemCreated(savedItem);
            return ResponseEntity.ok(savedItem);
        }).orElse(ResponseEntity.notFound().build());
    }


    @PatchMapping(
            value = "/{listId}/item/{listItemId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListItemEntity> editListItem(
            @PathVariable Long listId,
            @PathVariable Long listItemId,
            @RequestBody(required = false) ListItemRequestBody listItem) {
        if (!listItemEntityRepository.existsListByIdAndListId(listItemId, listId)) {
            return ResponseEntity.notFound().build();
        }
        if (listItem == null ||
                (listItem.getListItemName() == null && listItem.getListItemDescription() == null)) {
            return ResponseEntity
                    .of(ProblemDetail.forStatusAndDetail(
                            HttpStatus.BAD_REQUEST,
                            "No fields to update were provided"))
                    .build();
        }
        return listItemEntityRepository.findById(listItemId)
                .map(listItemEntity -> {
                    Optional.ofNullable(listItem.getListItemName())
                            .ifPresent(listItemEntity::setListItemName);
                    Optional.ofNullable(listItem.getListItemDescription())
                            .ifPresent(listItemEntity::setListItemDesc);
                    ListItemEntity updatedItem = listItemEntityRepository.save(listItemEntity);
                    listUpdatePublisher.publishListItemUpdated(updatedItem);
                    return ResponseEntity.ok(updatedItem);
                }).orElse(ResponseEntity.of(
                        ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Problem updating list item"
                        )).build());
    }

    @PatchMapping(
            value = "/{listId}/item/{listItemId}/complete",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListItemEntity> markItemComplete(
        @PathVariable Long listId,
        @PathVariable Long listItemId,
        @RequestParam boolean completed)
    {
        if(!listItemEntityRepository.existsListByIdAndListId(listItemId, listId)) {
            return ResponseEntity.notFound().build();
        }

        return listItemEntityRepository.findById(listItemId)
                .map(listItemEntity -> {
                    listItemEntity.setCompleted(completed);
                    listItemEntity.setCompletedOn(completed ? LocalDateTime.now() : null);
                    ListItemEntity updatedItem = listItemEntityRepository.save(listItemEntity);
                    listUpdatePublisher.publishListItemUpdated(updatedItem);
                    return ResponseEntity.ok(updatedItem);
                })
                .orElse(ResponseEntity.internalServerError().build());

    }

    @DeleteMapping(
            value = "/{listId}/item/{listItemId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListItemEntity> deleteListItem(@PathVariable Long listId, @PathVariable Long listItemId) {
        if (!listItemEntityRepository.existsListByIdAndListId(listItemId, listId)) {
            return ResponseEntity.notFound().build();
        }
        return listItemEntityRepository.findById(listItemId)
                .map(listItemEntity -> {
                    listItemEntityRepository.delete(listItemEntity);
                    listUpdatePublisher.publishListItemDeleted(listItemEntity);
                    return ResponseEntity.ok(listItemEntity);
                }).orElse(ResponseEntity.of(
                        ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Problem deleting list item"
                        )).build());
    }

    /***********************************************************
     * List sharing handling
     *********************************************************/

    // Create a shareable token for a list
    @PostMapping(value = "/{listId}/share", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createShare (@PathVariable Long listId){
        Account current_account = getCurrentAccount();
        if (current_account == null || current_account.getUser_id().equals(0L)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return listEntityRepository.findById(listId)
                    .map(list_entity -> {

                        // Ensure that the owner of the list is the current user.
                        if (!list_entity.getAccount().getUser_id().equals(current_account.getUser_id())) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                        }

                        // Todo: Change the default expiry time to a configurable value
                        ShareEntity new_share = new ShareEntity();
                        new_share.setList_id(list_entity.getId());
                        new_share = shareRepository.save(new_share);

                        listUpdatePublisher.publishShareCreated(list_entity);
                        return ResponseEntity.ok().body(Map.of("token", new_share.getShare_id(), "expiryTime", new_share.getExpiry_time(), "link", "list/accept/" + new_share.getShare_id(), "share", new_share));
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping(value = "/share/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteShare (@PathVariable String token){
        Account current_account = getCurrentAccount();

        try{
            return shareRepository.findById(UUID.fromString(token)).map(cur_share -> {
                // Check if the list exists.
                return listEntityRepository.findById(cur_share.getList_id())
                        .map(list_entity -> {
                            Account owner_account = list_entity.getAccount();
                            Account shared_to = cur_share.getUser();

                            //Determine if the current user has rights to delete the share.
                            if (list_entity.getAccount().getUser_id().equals(current_account.getUser_id())) {
                                // List owner.
                                if(shared_to == null) {
                                    listUpdatePublisher.publishShareDeleted(list_entity, owner_account.getUsername(), "");
                                }
                                else {
                                    listUpdatePublisher.publishShareDeleted(list_entity, owner_account.getUsername(), shared_to.getUsername());
                                }
                            }
                            else if (shared_to != null && current_account.getUser_id().equals(shared_to.getUser_id())) {
                                // User who the list was shared with.
                                listUpdatePublisher.publishShareRejected(list_entity, owner_account.getUsername(), shared_to.getUsername());
                            }
                            else {
                                // Not the owner of the list and not the user who the list was shared with.
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                            }

                            shareRepository.delete(cur_share);
                            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                        })
                        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());


        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Handle accepting a share
    @GetMapping(value = "/accept/{token}")
    public ResponseEntity<?> acceptShare(@PathVariable String token) {
        Account current_account = getCurrentAccount();

        try{
            return shareRepository.findById(UUID.fromString(token)).map(cur_share -> {
                        // Check if the share has expired
                        if (cur_share.getExpiry_time().toInstant().isBefore(Instant.now())) {
                            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                                    .header("Location", "/?error=Share authorization has expired. Contact the owner to request a new share link.")
                                    .build();
                        }

                        //No reusing the same share link twice.
                        if (cur_share.getUser() != null) {
                            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                                    .header("Location", "/?error=Share authorization has already been accepted.")
                                    .build();
                        }

                        return listEntityRepository.findById(cur_share.getList_id())
                                .map(list_entity -> {
                                    Account owner = list_entity.getAccount();
                                    // Check if the owner is also the current user - like they are testing the share link.
                                    if (owner.getUser_id().equals(current_account.getUser_id())) {
                                        return ResponseEntity.status(HttpStatus.FOUND)
                                                .header("Location", "/?v=" + cur_share.getList_id() + "&share=true")
                                                .build();
                                    }

                                    cur_share.setUser(current_account);
                                    shareRepository.save(cur_share);
                                    listUpdatePublisher.publishShareAccepted(list_entity, owner.getUsername(), current_account.getUsername());
                                    return ResponseEntity.status(HttpStatus.FOUND)
                                            .header("Location", "/?v=" + cur_share.getList_id() + "&note=You have been granted access to this shared list.")
                                            .build();
                                })
                                .orElse(ResponseEntity.status(HttpStatus.SEE_OTHER)
                                        .header("Location", "/?error=Shared list has been deleted.")
                                        .build());
            })
                    .orElse(ResponseEntity.status(HttpStatus.SEE_OTHER)
                            .header("Location", "/?error=Share offer has been deleted.")
                            .build());


        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
