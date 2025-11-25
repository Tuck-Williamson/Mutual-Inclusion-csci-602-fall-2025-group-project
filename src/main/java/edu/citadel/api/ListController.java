package edu.citadel.api;

import edu.citadel.api.request.CreateListRequest;
import edu.citadel.api.request.ListItemRequestBody;
import edu.citadel.api.websocket.ListUpdatePublisher;
import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.ListItemEntityRepository;
import edu.citadel.dal.model.ListEntity;
import edu.citadel.dal.model.ListItemEntity;
import lombok.Data;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.time.Instant;
import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/list")
public class ListController {

    private final ListEntityRepository listEntityRepository;

    private final ListItemEntityRepository listItemEntityRepository;
    
    private final ListUpdatePublisher listUpdatePublisher;

    @Autowired
    public ListController(
            final ListEntityRepository listEntityRepository,
            final ListItemEntityRepository listItemEntityRepository,
            final ListUpdatePublisher listUpdatePublisher) {
        this.listEntityRepository = listEntityRepository;
        this.listItemEntityRepository = listItemEntityRepository;
        this.listUpdatePublisher = listUpdatePublisher;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ListEntity>> getAllLists(@AuthenticationPrincipal OAuth2User principal
    ) {
        String username = "Guest";

        if (principal != null) {
            String login = principal.getAttribute("login");
            if (login != null && !login.isBlank()) {
                username = login;
            }
        }

        List<ListEntity> userLists = listEntityRepository.findByOwnerUsername(username);
        return ResponseEntity.ok(userLists);
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> createList(
            @RequestBody(required = false) CreateListRequest body,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        String username = "Guest";

        if (principal != null) {
            String login = principal.getAttribute("login");
            if (login != null && !login.isBlank()) {
                username = login;
            }
        }

        ListEntity list = new ListEntity();

        if (body != null && body.getTitle() != null && !body.getTitle().isBlank()) {
            list.setTitle(body.getTitle());
        }

        list.setOwnerUsername(username);

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
                    .map(ResponseEntity::ok)
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
    public ResponseEntity<?> createShare (@PathVariable Long listId, @AuthenticationPrincipal OAuth2User principal){
        if (principal == null || principal.getAuthorities().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Ensure we have a user ID to associate with the share
            String userId = principal.getAttribute("login");
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return listEntityRepository.findById(listId)
                    .map(listEntity -> {
                        // Todo: Change the default expiry time to a configurable value
                        Instant expiryTime = Instant.now().plusSeconds(300);

                        // String tokenData = userId + listId.toString() + expiryTime.toString();
                        // MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        // byte[] hash = digest.digest(tokenData.getBytes(StandardCharsets.UTF_8));
                        // String token = HexFormat.of().formatHex(hash);
                        // String token = UUID.randomUUID().toString();

                        // For now build a token that contains the info we need to simulate the lookup.
                        String token = "o-%s~i-%s~e-%s".formatted(userId, listId.toString(), expiryTime.toString());

                        listUpdatePublisher.publishShareCreated(listEntity);
                        // Todo: Store the token in the database once user authentication is implemented.
                        return ResponseEntity.ok().body(Map.of("token", token, "expiryTime", expiryTime, "link", "list/accept/" + token));
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping(value = "/share/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteShare (@PathVariable String token, @AuthenticationPrincipal OAuth2User principal){
        if (principal == null || principal.getAuthorities().isEmpty() || principal.getAttribute("login") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (token == null || token.isBlank()) {
            // If it's a bad request, I'm okay with it not being user-friendly.
            return ResponseEntity.badRequest().build();
        }
        try{
            String acting_user = principal.getAttribute("login");

            // Todo: replace with DB stuff once auth is worked out.
            String[] split = token.split("~");
            if (split.length != 3) {
                return ResponseEntity.badRequest().build();
            }
            String ownerId = split[0].substring(2);
            Long listId = Long.parseLong(split[1].substring(2));
            Instant expiryTime = Instant.parse(split[2].substring(2));

            // Check if the list exists.
            return listEntityRepository.findById(listId)
                    .map(listEntity -> {
                        //Determine if this is the user who owns the list.
                        if (ownerId.equals(acting_user)) {
                            // Todo: change acting_user below to reflect the user being dropped from sharing.
                            listUpdatePublisher.publishShareDeleted(listEntity, ownerId, acting_user);
                        }
                        else {
                            listUpdatePublisher.publishShareRejected(listEntity, ownerId, acting_user);
                        }
                        // Todo: remove share in DB.
                        return ResponseEntity.status(HttpStatus.FOUND).build();
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
    public ResponseEntity<?> acceptShare(@PathVariable String token, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || principal.getAuthorities().isEmpty() || principal.getAttribute("login") == null) {
            // This is an endpoint that should redirect to UI.
            // Thus, we cannot simply return HttpStatus.UNAUTHORIZED.
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header("Location", "/?error=You must be logged in to accept shared lists. Click the login button at the top right of the page.")
                    .build();
        }
        if (token == null || token.isBlank()) {
            // If it's a bad request, I'm okay with it not being user-friendly.
            return ResponseEntity.badRequest().build();
        }
        try{
            // Todo: replace with DB stuff once auth is worked out.
            String[] split = token.split("~");
            if (split.length != 3) {
                return ResponseEntity.badRequest().build();
            }
            String ownerId = split[0].substring(2);
            Long listId = Long.parseLong(split[1].substring(2));
            Instant expiryTime = Instant.parse(split[2].substring(2));

            // Check if the share has expired
            if (expiryTime.isBefore(Instant.now())) {
                return ResponseEntity.status(HttpStatus.SEE_OTHER)
                        .header("Location", "/?error=Share authorization has expired. Contact the owner to request a new share link.")
                        .build();
            }

            return listEntityRepository.findById(listId)
                    .map(listEntity -> {
                        // Check if the owner is also the current user - like they are testing the share link.
                        if (ownerId.equals(principal.getAttribute("login"))) {
                            return ResponseEntity.status(HttpStatus.FOUND)
                                    .header("Location", "/?v=" + listId + "&share=true")
                                    .build();
                        }

                        // Todo: store share in DB.
                        listUpdatePublisher.publishShareAccepted(listEntity, ownerId, principal.getAttribute("login"));
                        return ResponseEntity.status(HttpStatus.FOUND)
                                .header("Location", "/?v=" + listId + "&note=You have been granted access to this shared list.")
                                .build();
                    })
                    .orElse(ResponseEntity.status(HttpStatus.SEE_OTHER)
                            .header("Location", "/?error=Shared list has been deleted.")
                            .build());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
