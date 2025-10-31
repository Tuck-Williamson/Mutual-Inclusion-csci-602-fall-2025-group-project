package edu.citadel.api;

import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.model.ListEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListEntity> deleteList(@PathVariable Long id) {
        ListEntity list = this.repository.findById(id).orElse(null);
        if (list != null) {
            // This is to make sure that the list can be serialized to JSON.
            // The list items collection is lazily loaded, so there to populate the list we query the size.
            // Otherwise, because the list is deleted, the data object can't lazy load the list items when serialization occurs.
            //noinspection ResultOfMethodCallIgnored
            list.getListItems().size();

            this.repository.delete(list);
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
