package edu.citadel.api;

import edu.citadel.api.request.CreateListRequest;
import edu.citadel.api.request.ListItemRequestBody;
import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.ListItemEntityRepository;
import edu.citadel.dal.model.ListEntity;
import edu.citadel.dal.model.ListItemEntity;
import lombok.Data;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLDataException;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/list")
public class ListController {

    private final ListEntityRepository listEntityRepository;

    private final ListItemEntityRepository listItemEntityRepository;

    @Autowired
    public ListController(
            final ListEntityRepository listEntityRepository,
            final ListItemEntityRepository listItemEntityRepository) {
        this.listEntityRepository = listEntityRepository;
        this.listItemEntityRepository = listItemEntityRepository;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> createList(@RequestBody(required = false) CreateListRequest body) {
        ListEntity list = new ListEntity();

        if (body != null && body.getTitle() != null && !body.getTitle().isBlank()) {
            list.setTitle(body.getTitle());
        }

        return ResponseEntity.ok(listEntityRepository.save(list));
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
            return listItemEntityRepository.save(listItemEntity);
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
                    return ResponseEntity.ok(listItemEntityRepository.save(listItemEntity));
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
                    return ResponseEntity.ok(listItemEntityRepository.save(listItemEntity));
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
                    return ResponseEntity.ok(listItemEntity);
                }).orElse(ResponseEntity.of(
                        ProblemDetail.forStatusAndDetail(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Problem deleting list item"
                        )).build());
    }

}
