package edu.citadel.api;

import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.model.ListEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/list")
public class ListController {

    private final ListEntityRepository repository;

    @Autowired
    public ListController(final ListEntityRepository repository) {
        this.repository = repository;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> createList() {
        ListEntity list = new ListEntity();

        return ResponseEntity.ok(repository.save(list));
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
            return repository.findById(listId)
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
            return repository.findById(listId)
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

                        ListEntity updatedList = repository.save(existingList);
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
    public static class UpdateListRequest {
        private String title;
        private java.time.Instant completedOn;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public java.time.Instant getCompletedOn() {
            return completedOn;
        }

        public void setCompletedOn(java.time.Instant completedOn) {
            this.completedOn = completedOn;
        }
    }

}
